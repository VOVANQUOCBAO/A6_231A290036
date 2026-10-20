package vn.edu.vhu.ltdd.a6controls;
 
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
 
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
 
/** Màn hình xác nhận: chỉ hiển thị bản tóm tắt nhận từ màn hình đăng ký. */
public class ConfirmActivity extends AppCompatActivity {
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
 
        TextView tvTomTat = findViewById(R.id.tvTomTat);
        Button btnQuayLai = findViewById(R.id.btnQuayLai);
 
        String tomTat = getIntent().getStringExtra(MainActivity.EXTRA_TOM_TAT);
        tvTomTat.setText(tomTat != null ? tomTat : getString(R.string.no_data));
 
        btnQuayLai.setOnClickListener(v -> finish());  // đóng màn hình, quay về form
    }
}