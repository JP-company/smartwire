import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:smartwire_mobile/firebase/config/notification_config.dart';
import 'package:smartwire_mobile/firebase/firebase_options.dart';
import 'package:smartwire_mobile/login_page.dart';
import 'package:smartwire_mobile/home_page.dart';
import 'package:provider/provider.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';

import 'dto/jwt_dto.dart';
import 'local_storage/local_storage.dart';
import 'package:http/http.dart' as http;

Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  print("백그라운드 메시지 수신: ${message.notification!.title}, ${message.notification!.body}");
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
    options:DefaultFirebaseOptions.currentPlatform
  );

  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
  /// 알림 설정 최초 초기화
  NotificationConfig.initialize();

  JwtDto? jwtDto = await autoLogin();
  jwtDto = (jwtDto == null) ? JwtDto() : jwtDto;
  runApp(
      MyApp(jwtDto: jwtDto)
      // GetMaterialApp(
      //   home: MyApp(jwtDto: jwtDto),
      //   // initialBinding: BindingsBuilder.put(() => NotificationController(), permanent: true),
      // )
  );
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key, required this.jwtDto}) : super(key: key);
  final JwtDto jwtDto;

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (context) => jwtDto)
      ],
      child: MaterialApp(
        title: "SMART WIRE MOBILE APP",
        theme: ThemeData(
          primarySwatch: Colors.green,
        ),
        home: jwtDto.jwtMemberDto == null ? LoginPage() : HomePage(),
      ),
    );
  }
}

Future<JwtDto?> autoLogin() async {
  Future<dynamic> future = LocalStorage.load("jwt");
  var jwt = await future;

  if (jwt != null) {
    Map<String, String> headers = {
      'Authorization': jwt
    };

    final response = await http.get(
      Uri.parse('https://smartwire-backend-f39394ac6218.herokuapp.com/api/auto_login'),
      headers: headers,
    );

    if (response.statusCode == 200) {
      return JwtDto.fromJson(json.decode(response.body));; // 자동 로그인 성공
    }
  }
  return null;
}