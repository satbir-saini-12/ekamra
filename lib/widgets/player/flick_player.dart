import 'dart:async';
import 'dart:io';
import 'package:flick_video_player/flick_video_player.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hive/hive.dart';
import 'package:oxoo/models/videos.dart';
import 'package:video_player/video_player.dart';
import 'package:http/http.dart' as http;
import 'package:visibility_detector/visibility_detector.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import '../../constants.dart';
import '../../service/link_parser/link_parser.dart';
import '../../strings.dart';
import '../../style/theme.dart';

class FlickPlayer extends StatefulWidget {
  final String url;
  final String type;
  final bool isFullScreen;
  final List<Subtitle>? subtitles;

  const FlickPlayer({
    Key? key,
    required this.type,
    this.isFullScreen = true,
    required this.url,
    this.subtitles,
  }) : super(key: key);

  @override
  State<FlickPlayer> createState() => _FlickPlayerState();
}

class _FlickPlayerState extends State<FlickPlayer> {
  FlickManager? flickManager;
  bool isDark = false;
  VideoPlayerController? _videoController;
  bool _isInitialized = false;
  bool _isPreparing = false;
  String? _preparedUrl;
  Future<String>? _linkFuture;

  @override
  void initState() {
    super.initState();
    isDark = Hive.box('appModeBox').get('isDark') ?? false;
    // Prepare link once in initState
    _linkFuture = LinkParser().getPlayableLink(
      linkType: widget.type,
      url: widget.url,
    );
  }

  Future<void> _prepareFlickManager(String url) async {
    // Prevent multiple simultaneous initializations
    if (_isPreparing || _preparedUrl == url) {
      return;
    }

    _isPreparing = true;
    _preparedUrl = url;

    try {
      final uri = Uri.parse(url);

      // Dispose previous controller if exists
      if (_videoController != null) {
        await _videoController!.dispose();
        _videoController = null;
      }

      // Dispose previous flickManager if exists
      if (flickManager != null) {
        flickManager!.dispose();
        flickManager = null;
      }

      // Create video controller
      _videoController = VideoPlayerController.networkUrl(
        uri,
        httpHeaders: {
          'User-Agent': 'Mozilla/5.0',
        },
        closedCaptionFile: _loadCaptions(),
      );

      // Initialize video controller
      await _videoController!.initialize().timeout(
        const Duration(seconds: 30),
        onTimeout: () {
          throw TimeoutException("Video initialization timeout");
        },
      );

      if (mounted && _videoController != null) {
        // Create FlickManager - minimal configuration
        flickManager = FlickManager(
          videoPlayerController: _videoController!,
        );

        setState(() {
          _isInitialized = true;
          _isPreparing = false;
        });

        // Auto-play after initialization
        Future.delayed(const Duration(milliseconds: 500), () {
          if (mounted &&
              flickManager?.flickVideoManager?.videoPlayerController != null) {
            flickManager?.flickVideoManager?.videoPlayerController?.play();
          }
        });
      } else {
        _isPreparing = false;
      }
    } catch (e) {
      printLog('Failed to initialize video player: $e');
      if (mounted) {
        setState(() {
          _isInitialized = false;
          _isPreparing = false;
        });
      }
    }
  }

  Future<ClosedCaptionFile> _loadCaptions() async {
    if (widget.subtitles == null || widget.subtitles!.isEmpty) {
      return SubRipCaptionFile('');
    }

    try {
      final url = Uri.parse(widget.subtitles![0].url.toString());
      final data = await http.get(url);
      final srtContent = data.body.toString();
      printLog("-----vtt file read: $srtContent");
      flickManager?.flickControlManager?.toggleSubtitle();
      return WebVTTCaptionFile(srtContent);
    } catch (e) {
      printLog('---------Failed to get subtitles for $e');
      return SubRipCaptionFile('');
    }
  }

  Widget _buildVideoPlayer() {
    if (!_isInitialized || flickManager == null) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    // Use FlickVideoPlayer exactly like the working project
    // Let it handle fullscreen natively - it works correctly there
    // Wrap in AbsorbPointer to prevent touch event conflicts during initialization
    return AbsorbPointer(
      absorbing: _isPreparing,
      child: FlickVideoPlayer(
        flickManager: flickManager!,
        flickVideoWithControls: FlickVideoWithControls(
          videoFit: BoxFit.contain, // Critical: prevents stretching
          controls: FlickPortraitControls(
            progressBarSettings: FlickProgressBarSettings(
              playedColor: Colors.red,
            ),
          ),
          closedCaptionTextStyle: const TextStyle(fontSize: 20),
        ),
      ),
    );
  }

  Future<void> setAllOrientations() async {
    await SystemChrome.setEnabledSystemUIMode(
      SystemUiMode.manual,
      overlays: SystemUiOverlay.values,
    );
    await SystemChrome.setPreferredOrientations(DeviceOrientation.values);
    await WakelockPlus.disable();
  }

  @override
  void dispose() {
    printLog("-------------flick player dispose()");
    _isPreparing = false;
    Future.delayed(Duration.zero, () async {
      await setAllOrientations();
      flickManager?.dispose();
      flickManager = null;
      _videoController?.dispose();
      _videoController = null;
    });
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!Platform.isIOS) {
      return _mainUI(context);
    } else {
      return GestureDetector(
        onPanUpdate: (details) {
          if (details.delta.dx < 0) {
            Navigator.pop(context, true);
          }
        },
        child: _mainUI(context),
      );
    }
  }

  Widget _mainUI(BuildContext context) {
    final isEmbedded = !widget.isFullScreen;

    Widget body = FutureBuilder<String>(
      future: _linkFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(
            child: CircularProgressIndicator(),
          );
        }

        if (snapshot.connectionState == ConnectionState.done) {
          if (snapshot.hasData) {
            String? link = snapshot.data;
            // Only prepare if not already prepared or if URL changed
            if (link != null && (_preparedUrl != link || !_isInitialized)) {
              // Use WidgetsBinding to ensure this runs after build
              WidgetsBinding.instance.addPostFrameCallback((_) {
                if (mounted) {
                  _prepareFlickManager(link);
                }
              });
            }

            if (!_isInitialized) {
              return const Center(
                child: CircularProgressIndicator(),
              );
            }

            return WillPopScope(
              onWillPop: () async {
                Navigator.pop(context, false);
                return true;
              },
              child: VisibilityDetector(
                key: ObjectKey(flickManager),
                onVisibilityChanged: (visibility) {
                  // Only handle visibility changes when fully visible or fully hidden
                  // This prevents flickering from partial visibility changes
                  if (!mounted || flickManager == null) return;
                  
                  if (visibility.visibleFraction == 0) {
                    flickManager?.flickControlManager?.autoPause();
                  } else if (visibility.visibleFraction >= 0.9) {
                    // Only resume when almost fully visible to prevent flickering
                    flickManager?.flickControlManager?.autoResume();
                  }
                },
                child: _buildVideoPlayer(),
              ),
            );
          } else {
            return Center(
              child: Text(
                AppContent.noItemFound,
                textAlign: TextAlign.center,
                style:
                    isDark ? CustomTheme.bodyText3White : CustomTheme.bodyText3,
              ),
            );
          }
        }

        return Center(
          child: Text(
            AppContent.noItemFound,
            textAlign: TextAlign.center,
            style: isDark ? CustomTheme.bodyText3White : CustomTheme.bodyText3,
          ),
        );
      },
    );

    if (isEmbedded) {
      // For embedded view, return body directly without Scaffold
      return body;
    } else {
      return Scaffold(
        backgroundColor: isDark ? CustomTheme.primaryColorDark : Colors.white,
        appBar: AppBar(
          iconTheme: IconThemeData(
            color: isDark
                ? CustomTheme.primaryColorRed
                : CustomTheme.primaryColorDark,
          ),
          titleTextStyle: TextStyle(
            fontSize: 24,
            color: isDark
                ? CustomTheme.primaryColorRed
                : CustomTheme.primaryColorDark,
          ),
          backgroundColor: CustomTheme.colorAccentDark,
          title: Text(AppContent.goBack),
        ),
        body: SafeArea(
          child: body,
        ),
      );
    }
  }
}
