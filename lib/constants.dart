import 'package:flutter/foundation.dart';

const String kLOG_TAG = "[APP]";
const bool kLOG_ENABLE = !kReleaseMode;

printLog(dynamic data) {
  if (kLOG_ENABLE) {
    print("$kLOG_TAG${data.toString()}");
  }
}
