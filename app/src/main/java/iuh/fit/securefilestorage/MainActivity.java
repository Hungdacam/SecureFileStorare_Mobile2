package iuh.fit.securefilestorage;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKeys;

import android.Manifest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;



public class MainActivity extends AppCompatActivity {
    private Button btnSave;
    private Button btnRead;
    private EditText editText;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        requestStoragePermission();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initComponents();
    }

    private void initComponents(){
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);
        btnSave = findViewById(R.id.btnSave);
        btnRead = findViewById(R.id.btnRead);
        btnRead.setOnClickListener(v -> readFromFile("secure_text_encrypted.txt"));
        btnSave.setOnClickListener(v -> {
            String text = editText.getText().toString();
            File path = new File(Environment.getExternalStorageDirectory(), "SecureFiles");
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, "secure_text_encrypted.txt");
            encryptFile(file, text);
        });

    }

    private static final int REQUEST_PERMISSION = 1;

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền lưu trữ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Từ chối quyền lưu trữ", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void readFromFile(String filename) {
        try {
            File path = new File(Environment.getExternalStorageDirectory(), "SecureFiles");
            File file = new File(path, filename);

            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            fis.close();

            textView.setText(sb.toString());
            Toast.makeText(this, "Đọc file thành công!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi đọc file", Toast.LENGTH_SHORT).show();
        }
    }
    private void encryptFile(File file, String content) {
        try {
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    file,
                    this,
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            FileOutputStream fos = encryptedFile.openFileOutput();
            fos.write(content.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String decryptFile(File file) {
        try {
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    file,
                    this,
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            FileInputStream fis = encryptedFile.openFileInput();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            fis.close();
            return byteArrayOutputStream.toString(); // Chuyển byte[] thành chuỗi

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Xử lý lỗi nếu có
        }
    }

}