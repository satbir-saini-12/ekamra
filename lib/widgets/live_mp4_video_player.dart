import 'package:chewie/chewie.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hive/hive.dart';
import 'package:video_player/video_player.dart';

class VideoPlayerWidget extends StatefulWidget {
  final String? videoUrl;

  const VideoPlayerWidget({Key? key, this.videoUrl}) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    return _VideoPlayerWidgetState();
  }
}

class _VideoPlayerWidgetState extends State<VideoPlayerWidget> {
  late VideoPlayerController _videoPlayerController1;
  ChewieController? _chewieController;
  static bool? isDark;
  var appModeBox = Hive.box('appModeBox');
  double? _aspectRatio;

  @override
   initState() {
    super.initState();
    isDark = appModeBox.get('isDark') ?? false;
    print(widget.videoUrl);
    _videoPlayerController1 = VideoPlayerController.network(widget.videoUrl!);
    _initializePlayer();
  }

  Future<void> _initializePlayer() async {
    await _videoPlayerController1.initialize();
    
    // Get actual aspect ratio from video
    _aspectRatio = _videoPlayerController1.value.aspectRatio;
    if (_aspectRatio == null || _aspectRatio!.isNaN || _aspectRatio! <= 0) {
      _aspectRatio = 16 / 9; // Default to 16:9 if aspect ratio is invalid
    }
    
    if (mounted) {
      setState(() {
        _chewieController = ChewieController(
          videoPlayerController: _videoPlayerController1,
          aspectRatio: _aspectRatio!,
          autoPlay: true,
          looping: true,
          isLive: true,
          allowFullScreen: true,
          fullScreenByDefault: false,
          allowMuting: true,
          allowPlaybackSpeedChanging: false,
          errorBuilder: (context, errorMessage) {
            return Center(
              child: Text(
                errorMessage.isNotEmpty ? errorMessage : "Invalid Live TV URL",
                style: TextStyle(color: Colors.white),
              ),
            );
          },
        );
      });
    }
  }

  @override
  void dispose() {
    _chewieController?.dispose();
    _videoPlayerController1.dispose();
    super.dispose();
  }

  // @override
  // Widget build(BuildContext context) {
  //   return Chewie(
  //     controller: _chewieController,
  //   );
  // }

  @override
  Widget build(BuildContext context) {
    if (_chewieController == null) {
      return Center(
        child: CircularProgressIndicator(),
      );
    }

    Size size = MediaQuery.of(context).size;
    var topbarContainerHeight = size.height * 0.15;

    return WillPopScope(
      onWillPop: () async {
        if (_chewieController?.isFullScreen == true) {
          _chewieController?.exitFullScreen();
          await SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
          return false;
        }
        await SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
        return true;
      },
      child: Container(
        child: Stack(
          children: <Widget>[
            Container(
              // Use the VideoPlayer widget to display the video.
              child: Chewie(
                controller: _chewieController!,
              ),
            ),
            if (!_chewieController!.isFullScreen)
              Container(
                height: topbarContainerHeight,
                alignment: Alignment.bottomLeft,
                margin: EdgeInsets.only(left: 20, right: 20),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    InkWell(
                      child: Icon(Icons.arrow_back_ios, color: isDark! ? Colors.white : Colors.black),
                      onTap: () {
                        SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
                        Navigator.pop(context, true);
                      },
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}
