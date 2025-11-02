package com.mss.project.user_service.utils;

public class EmailUtils {

    public static String subjectRegister(){
        return "Chào mừng bạn đã đăng ký thành công tài khoản 4Bus";
    }

    public static String getWelcomeEmailContent() {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="UTF-8" />
              <style>
                body { font-family: 'Segoe UI', sans-serif; background: #f5f7fa; margin: 0; padding: 0; }
                .container { max-width: 600px; margin: auto; background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); overflow: hidden; }
                .header { background: #3f51b5; color: white; padding: 40px 30px; text-align: center; }
                .body { padding: 30px; color: #333; }
                .body h2 { font-size: 24px; }
                .body p { font-size: 16px; line-height: 1.6; }
                .cta-button { display: inline-block; margin-top: 20px; padding: 14px 24px; background: #3f51b5; color: white; text-decoration: none; border-radius: 8px; }
                .footer { text-align: center; padding: 20px; font-size: 13px; color: #999; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>Chào mừng đến với 4Bus</h1>
                </div>
                <div class="body">
                  <h2>Xin chào 👋</h2>
                  <p>Chúng tôi rất vui khi bạn đã gia nhập cộng đồng 4Bus! Hãy khám phá những tiện ích tuyệt vời từ nền tảng của chúng tôi.</p>
                  <p>Bắt đầu khám phá trang cá nhân của bạn ngay bây giờ.</p>
                  <a href="https://your-app-url.com" class="cta-button">Khám phá ngay</a>
                </div>
                <div class="footer">© 2025 4Bus. Mọi quyền được bảo lưu.</div>
              </div>
            </body>
            </html>
            """;
    }
}
