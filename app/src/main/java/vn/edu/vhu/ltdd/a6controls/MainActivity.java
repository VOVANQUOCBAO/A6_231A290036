package vn.edu.vhu.ltdd.a6controls;
 
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
 
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
 
import com.google.android.material.materialswitch.MaterialSwitch;
 
import java.util.ArrayList;
import java.util.List;
 
public class MainActivity extends LabActivity {
 
    private static final String TAG = "A6_231A290036";
 
    public static final String EXTRA_TOM_TAT = "extra_tom_tat";
 
    private EditText edtHoTen, edtMssv;
    private Spinner spMonHoc;
    private int pendingCourse=-1;
    private RadioGroup rgHeDaoTao;
    private CheckBox cbSang, cbChieu, cbToi;
    private MaterialSwitch swThongBao;
    private ToggleButton tgUuTien;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showLayout(R.layout.activity_main);

        edtHoTen = findViewById(R.id.edtHoTen);
        edtMssv = findViewById(R.id.edtMssv);
        spMonHoc = findViewById(R.id.spMonHoc);
        rgHeDaoTao = findViewById(R.id.rgHeDaoTao);
        cbSang = findViewById(R.id.cbSang);
        cbChieu = findViewById(R.id.cbChieu);
        cbToi = findViewById(R.id.cbToi);
        swThongBao = findViewById(R.id.swThongBao);
        tgUuTien = findViewById(R.id.tgUuTien);
        Button btnXacNhan = findViewById(R.id.btnXacNhan);
        Button btnLamLai = findViewById(R.id.btnLamLai);
 
        // ---- Spinner: đổ dữ liệu từ res/values/arrays.xml bằng ArrayAdapter ----
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.mon_hoc, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonHoc.setAdapter(adapter);
 
        spMonHoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Đã chọn học phần: " + parent.getItemAtPosition(position));
            }
 
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không dùng, nhưng vẫn phải cài đặt vì đây là phương thức của interface
            }
        });
 
        Spinner khoa=findViewById(R.id.spKhoa);
        ArrayAdapter<CharSequence> khoaAdapter=ArrayAdapter.createFromResource(this,R.array.khoa,android.R.layout.simple_spinner_item);
        khoaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        khoa.setAdapter(khoaAdapter);
        khoa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent,View view,int pos,long id) {
                int array=pos==0?R.array.mon_hoc:R.array.mon_kinh_te;
                // onRestoreInstanceState có thể vừa phục hồi Spinner học phần.
                Object selected=spMonHoc.getSelectedItem();
                String old=selected==null?"":selected.toString();
                ArrayAdapter<CharSequence> a=ArrayAdapter.createFromResource(MainActivity.this,array,android.R.layout.simple_spinner_item);
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spMonHoc.setAdapter(a);
                int index=a.getPosition(old); if(index>=0) spMonHoc.setSelection(index);
                if(pendingCourse>=0) { if(pendingCourse<a.getCount()) spMonHoc.setSelection(pendingCourse); pendingCourse=-1; }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        if(savedInstanceState!=null) {
            pendingCourse=savedInstanceState.getInt("course",0);
            khoa.setSelection(savedInstanceState.getInt("faculty",0));
        }
        android.widget.CompoundButton.OnCheckedChangeListener count=(button,checked)->capNhatSoBuoi();
        cbSang.setOnCheckedChangeListener(count); cbChieu.setOnCheckedChangeListener(count); cbToi.setOnCheckedChangeListener(count);
        capNhatSoBuoi();
        // ---- CheckBox: lắng nghe thay đổi ----

 
        // ---- RadioGroup: chỉ chọn được một ----
        rgHeDaoTao.setOnCheckedChangeListener((group, checkedId) -> {
            String he = (checkedId == R.id.rbChinhQuy) ? getString(R.string.he_chinh_quy)
                    : getString(R.string.he_vlvh);
            Log.d(TAG, "Hệ đào tạo: " + he);
        });
 
        // ---- Switch ----
        swThongBao.setOnCheckedChangeListener((buttonView, isChecked) ->
                Log.d(TAG, "Nhận thông báo: " + isChecked));
 
        btnXacNhan.setOnClickListener(v -> xacNhan());
        btnLamLai.setOnClickListener(v -> lamLai());
    }
 
    /** Kiểm tra dữ liệu rồi chuyển sang màn hình xác nhận. */
    private void xacNhan() {
        edtHoTen.setError(null); edtMssv.setError(null);
        String hoTen = edtHoTen.getText().toString().trim();
        String mssv = edtMssv.getText().toString().trim();
 
        if (hoTen.isEmpty()) {
            edtHoTen.setError(getString(R.string.err_empty));
            edtHoTen.requestFocus();
            return;
        }
        if (!mssv.matches("[0-9]{10}")) {
            edtMssv.setError(getString(R.string.err_mssv));
            edtMssv.requestFocus();
            return;
        }
        if (rgHeDaoTao.getCheckedRadioButtonId() == -1) {   // -1 = chưa chọn nút nào
            Toast.makeText(this, R.string.err_he, Toast.LENGTH_SHORT).show();
            return;
        }
 
        List<String> buoiHoc = new ArrayList<>();
        if (cbSang.isChecked()) buoiHoc.add(getString(R.string.buoi_sang));
        if (cbChieu.isChecked()) buoiHoc.add(getString(R.string.buoi_chieu));
        if (cbToi.isChecked()) buoiHoc.add(getString(R.string.buoi_toi));
        if (buoiHoc.isEmpty()) {
            Toast.makeText(this, R.string.err_buoi, Toast.LENGTH_SHORT).show();
            return;
        }
 
        String he = (rgHeDaoTao.getCheckedRadioButtonId() == R.id.rbChinhQuy)
                ? getString(R.string.he_chinh_quy) : getString(R.string.he_vlvh);
 
        String tomTat = getString(R.string.tom_tat_format,
                hoTen,
                mssv,
                spMonHoc.getSelectedItem().toString(),
                he,
                TextUtils.join(", ", buoiHoc),  // String.join() cần API 26 nên dùng TextUtils
                swThongBao.isChecked() ? getString(R.string.co) : getString(R.string.khong),
                tgUuTien.isChecked() ? getString(R.string.bat) : getString(R.string.tat));
 
        Intent intent = new Intent(this, ConfirmActivity.class);
        intent.putExtra(EXTRA_TOM_TAT, tomTat);
        startActivity(intent);
    }
 
    /** Đưa mọi control về trạng thái ban đầu. */
    private void lamLai() {
        edtHoTen.setText("");
        edtMssv.setText("");
        edtHoTen.setError(null);
        edtMssv.setError(null);
        ((Spinner)findViewById(R.id.spKhoa)).setSelection(0);
        spMonHoc.setSelection(0);
        rgHeDaoTao.clearCheck();
        cbSang.setChecked(false);
        cbChieu.setChecked(false);
        cbToi.setChecked(false);
        swThongBao.setChecked(true);
        tgUuTien.setChecked(false);
        edtHoTen.requestFocus();
    }

    private void capNhatSoBuoi() {
        int count=(cbSang.isChecked()?1:0)+(cbChieu.isChecked()?1:0)+(cbToi.isChecked()?1:0);
        ((android.widget.TextView)findViewById(R.id.tvSoBuoi)).setText(getString(R.string.session_count,count));
    }
    @Override protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state); capNhatSoBuoi();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("faculty",((Spinner)findViewById(R.id.spKhoa)).getSelectedItemPosition());
        state.putInt("course",spMonHoc.getSelectedItemPosition()); super.onSaveInstanceState(state);
    }
}
