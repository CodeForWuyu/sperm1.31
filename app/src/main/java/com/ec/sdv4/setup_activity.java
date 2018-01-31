package com.ec.sdv4;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class setup_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        TextView phone = (TextView)findViewById(R.id.phone);
        TextView mail = (TextView)findViewById(R.id.mail);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调到拨号界面
                Uri uri = Uri.parse("tel:123456");
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(intent);
            }
        });

        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822"); // 真机上使用这行
                i.putExtra(Intent.EXTRA_EMAIL, new String[] { "123456@qq.com" });
                i.putExtra(Intent.EXTRA_SUBJECT, "您的建议");
                i.putExtra(Intent.EXTRA_TEXT, "我们很希望能得到您的建议！！！");
                startActivity(Intent.createChooser(i, "选择邮箱应用..."));
            }
        });
    }
}

