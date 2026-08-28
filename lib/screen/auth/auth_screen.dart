import 'dart:async';
import 'dart:io';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../constants.dart';
import '../../utils/button_widget.dart';
import 'package:hive/hive.dart';
import 'package:provider/provider.dart';
import 'package:sign_in_with_apple/sign_in_with_apple.dart';
import '../../bloc/auth/firebase_auth/firebase_auth_bloc.dart';
import '../../bloc/auth/firebase_auth/firebase_auth_event.dart';
import '../../bloc/auth/firebase_auth/firebase_auth_state.dart';
import '../../models/user_model.dart';
import '../../screen/landing_screen.dart';
import '../../service/authentication_service.dart';
import '../../style/theme.dart';
import '../../config.dart';
import '../../strings.dart';
import '../phon_auth_screen.dart';
import 'signIn_screen.dart';

final FirebaseAuth _auth = FirebaseAuth.instance;

class AuthScreen extends StatefulWidget {
  static final String route = '/AuthScreen';
  final bool? fromPaidScreen;
  const AuthScreen({Key? key, this.fromPaidScreen}) : super(key: key);
  @override
  _AuthScreenState createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  late Bloc firebaseAuthBloc;
  bool isLoading = false;
  var appModeBox = Hive.box('appModeBox');
  late bool fromPaidScreen;
  late bool _isLogged;
  late bool isDark;

  @override
  void initState() {
    firebaseAuthBloc = BlocProvider.of<FirebaseAuthBloc>(context);
    fromPaidScreen = widget.fromPaidScreen ?? false;
    isDark = appModeBox.get('isDark') ?? false;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    printLog("_AuthScreenState");
    final AuthService authService = Provider.of<AuthService>(context);
    _isLogged = authService.getUser() != null ? true : false;

    return _isLogged
        ? LandingScreen()
        : Scaffold(
            appBar: fromPaidScreen
                ? AppBar(
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
                    backgroundColor: isDark
                        ? CustomTheme.darkGrey
                        : CustomTheme.primaryColor,
                    title: Text(AppContent.goBack))
                : PreferredSize(
                    preferredSize: Size.fromHeight(0.0),
                    child: Container(
                      width: 0.0,
                      height: 0.0,
                    )),
            backgroundColor:
                isDark ? CustomTheme.primaryColorDark : Colors.black,
            body: SingleChildScrollView(
              child: Stack(
                alignment: Alignment.center,
                children: [
                  Stack(
                    alignment: Alignment.bottomLeft,
                    children: [
                      Container(
                        width: MediaQuery.of(context).size.width,
                        height: MediaQuery.of(context).size.height,
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topLeft,
                            end: Alignment.bottomRight,
                            colors: [
                              Color(0xFFFFD700), // Gold/Yellow
                              Color(0xFFFFA500), // Orange
                              Color(0xFFFFD700), // Gold/Yellow
                            ],
                          ),
                        ),
                        child: Center(
                          child: Image.asset(
                            'assets/splash_image.png',
                            fit: BoxFit.contain,
                            width: 200,
                            height: 200,
                          ),
                        ),
                      ),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          if (Config.enablePhoneAuth)
                            phoneAuthWidget(
                              color: CustomTheme.springGreen,
                              title: AppContent.loginWithPhone,
                              imagePath: "ic_button_phone",
                            ),
                          // Temporarily disabled due to google_sign_in package compatibility issues
                          // if (Config.enableGoogleAuth)
                          //   socialAuthWidget(
                          //     color: CustomTheme.dodgerBlue,
                          //     title: AppContent.loginWithGoogle,
                          //     imagePath: "ic_button_google",
                          //     authService: authService,
                          //     function: _signInWithGoogle,
                          //   ),
                          InkWell(
                              onTap: () {
                                Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                        builder: (context) => LoginPage()));
                              },
                              child: mailAuthWidget(
                                  color: CustomTheme.salmonColor,
                                  title: AppContent.loginWithEmail,
                                  imagePath: "ic_button_email")),
                          if (Platform.isIOS)
                            socialAuthWidget(
                                color: Colors.white,
                                title: AppContent.loginWithApple,
                                imagePath: "ic_button_apple",
                                authService: authService,
                                function: _signInWithApple,
                                isApple: true),
                          HelpMe().space(90.0),
                        ],
                      ),
                    ],
                  ),
                  if (isLoading) CircularProgressIndicator(),
                ],
              ),
            ),
          );
  }

  Widget socialAuthWidget(
      {Color? color,
      String? title,
      String? imagePath,
      AuthService? authService,
      Function? function,
      bool isApple = false}) {
    return BlocListener<FirebaseAuthBloc, FirebaseAuthState>(
      listener: (context, state) {
        if (state is FirebaseAuthStateCompleted) {
          AuthUser? user = state.getUser;
          if (user == null) {
            firebaseAuthBloc.add(FirebaseAuthFailed);
          } else {
            // print(user.toJson().toString());
            authService!.updateUser(user);
            isLoading = false;
            if (authService.getUser() != null) {
              Navigator.of(context).pushAndRemoveUntil(
                  MaterialPageRoute(builder: (context) => LandingScreen()),
                  (Route<dynamic> route) => false);
            }
          }
        } else if (state is FirebaseAuthFailedState) {
          firebaseAuthBloc.add(FirebaseAuthNotStarted());
        }
      },
      child: BlocBuilder<FirebaseAuthBloc, FirebaseAuthState>(
        builder: (context, state) {
          return InkWell(
            onTap: () async {
              setState(() {
                isLoading = true;
              });
              User? fbUser = await function!();
              if (fbUser != null) {
                firebaseAuthBloc.add(FirebaseAuthStarted());
                firebaseAuthBloc.add(FirebaseAuthCompleting(
                  uid: fbUser.uid,
                  email: fbUser.email,
                  phone: fbUser.phoneNumber,
                ));
              } else {
                setState(() {
                  isLoading = false;
                });
              }
            },
            child: Padding(
              padding:
                  const EdgeInsets.symmetric(horizontal: 30.0, vertical: 10.0),
              child: Container(
                child: Padding(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 10.0, vertical: 10.0),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Image.asset(
                        'assets/images/$imagePath.png',
                        width: 20.0,
                        height: 20.0,
                      ),
                      SizedBox(
                        width: 10.0,
                      ),
                      Text(title!,
                          style: isApple
                              ? CustomTheme.authBtnTitleBlack
                              : CustomTheme.authBtnTitle),
                    ],
                  ),
                ),
                decoration: BoxDecoration(
                    color: color,
                    borderRadius: BorderRadius.all(Radius.circular(4.0))),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget phoneAuthWidget(
      {Color? color, required String title, String? imagePath}) {
    return InkWell(
      onTap: () {
        Navigator.pushNamed(context, PhoneAuthScreen.route);
      },
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 30.0, vertical: 10.0),
        child: Container(
          child: Padding(
            padding:
                const EdgeInsets.symmetric(horizontal: 10.0, vertical: 10.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Image.asset(
                  'assets/images/$imagePath.png',
                  width: 20.0,
                  height: 20.0,
                ),
                SizedBox(
                  width: 10.0,
                ),
                Text(
                  title,
                  style: CustomTheme.authBtnTitle,
                ),
              ],
            ),
          ),
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.all(Radius.circular(4.0)),
          ),
        ),
      ),
    );
  }

  Widget mailAuthWidget(
      {Color? color, required String title, String? imagePath}) {
    return InkWell(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 30.0, vertical: 10.0),
        child: Container(
          child: Padding(
            padding:
                const EdgeInsets.symmetric(horizontal: 10.0, vertical: 10.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Image.asset(
                  'assets/images/$imagePath.png',
                  width: 20.0,
                  height: 20.0,
                ),
                SizedBox(
                  width: 10.0,
                ),
                Text(
                  title,
                  style: CustomTheme.authBtnTitle,
                ),
              ],
            ),
          ),
          decoration: BoxDecoration(
            color: color,
            borderRadius: BorderRadius.all(Radius.circular(4.0)),
          ),
        ),
      ),
    );
  }

  /*
  Future<User?> _signInWithGoogle() async {
    try {
      final GoogleSignIn googleSignIn = GoogleSignIn.standard();
      final GoogleSignInAccount? account = await googleSignIn.signIn();
      if (account == null) return null;
      final GoogleSignInAuthentication authentication = await account.authentication;
      final AuthCredential credential = GoogleAuthProvider.credential(
        idToken: authentication.idToken,
      );
      final UserCredential userCredential = await _auth.signInWithCredential(credential);
      final User? user = userCredential.user;
      if (user != null) {
        assert(user.displayName != null);
        assert(!user.isAnonymous);
        final User currentUser = _auth.currentUser!;
        assert(user.uid == currentUser.uid);
      }
      return user;
    } catch (e) {
      print("google sign in error");
      print(e);
      return null;
    }
  }
  */

  Future<User?> _signInWithApple() async {
    try {
      final appleCredential = await SignInWithApple.getAppleIDCredential(
        scopes: [
          AppleIDAuthorizationScopes.email,
          AppleIDAuthorizationScopes.fullName,
        ],
      );
      print(appleCredential);
      final OAuthProvider oAuthProvider = OAuthProvider('apple.com');
      final credential = oAuthProvider.credential(
          idToken: appleCredential.identityToken,
          accessToken: appleCredential.authorizationCode);
      final User? user = (await _auth.signInWithCredential(credential)).user;
      if (user != null) {
        print(user.email);
        if (user.email != null && user.email != "") {
          assert(user.email != null);
        }
        if (user.displayName != null && user.displayName != "") {
          assert(user.displayName != null);
        }
        assert(!user.isAnonymous);

        final User currentUser = _auth.currentUser!;
        assert(user.uid == currentUser.uid);
      }
      return user;
    } catch (e) {
      print("apple sign in error");
      print(e);
      return null;
    }
  }
}
