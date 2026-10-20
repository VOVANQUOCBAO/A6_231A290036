package vn.edu.vhu.ltdd.a6controls;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;
import java.util.*;

/** Runs the submitted scenarios on the device; no test hooks in the release app. */
public class LabRunner extends Instrumentation {
    private volatile Activity active;
    private final ArrayList<String> results=new ArrayList<>();
    private String mode="full";
    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        if(arguments!=null) mode=arguments.getString("mode","full");
        start();
    }
    @Override public void onStart() {
        try {
            ((Application)getTargetContext().getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                public void onActivityResumed(Activity a){active=a;}
                public void onActivityCreated(Activity a,Bundle b){active=a;}
                public void onActivityStarted(Activity a){}
                public void onActivityPaused(Activity a){}
                public void onActivityStopped(Activity a){}
                public void onActivitySaveInstanceState(Activity a,Bundle b){}
                public void onActivityDestroyed(Activity a){}
            });
            Intent intent=new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            active=startActivitySync(intent); settle();
            execute();
            save("results.txt",android.text.TextUtils.join("\n",results)+"\nALL PASSED\n");
            Bundle done=new Bundle();done.putString("stream","\n"+android.text.TextUtils.join("\n",results)+"\nALL PASSED\n");finish(Activity.RESULT_OK,done);
        } catch(Throwable e) {
            StringWriter sw=new StringWriter();e.printStackTrace(new PrintWriter(sw));
            try { save("results.txt",android.text.TextUtils.join("\n",results)+"\nFAILED\n"+sw); shot("FAILED"); } catch(Throwable ignored){}
            Bundle b=new Bundle();b.putString("stream","FAILED: "+sw);finish(Activity.RESULT_CANCELED,b);
        }
    }
    private void ui(Runnable action) { runOnMainSync(action); waitForIdleSync(); }
    private void pause(long ms) { SystemClock.sleep(ms); }
    private void settle(){waitForIdleSync();pause(900);}
    private View view(int id){return active.findViewById(id);}
    private void hideKeyboard(){ui(()->((InputMethodManager)active.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(active.getWindow().getDecorView().getWindowToken(),0));}
    private void show(int id) {
        hideKeyboard(); ui(()->{View v=view(id);v.requestRectangleOnScreen(new Rect(0,0,v.getWidth(),v.getHeight()),true);});pause(300);
    }
    private void click(int id){show(id);ui(()->view(id).performClick());settle();}
    private void edit(int id,String value){ui(()->{
        EditText e=(EditText)view(id);int type=e.getInputType();
        e.setKeyListener(android.text.method.TextKeyListener.getInstance());e.setRawInputType(type);
        e.setText(value);
    });}
    private String text(int id){final String[] s={""};ui(()->s[0]=((TextView)view(id)).getText().toString());return s[0];}
    private String error(int id){final String[] s={""};ui(()->{CharSequence e=((TextView)view(id)).getError();s[0]=e==null?"":e.toString();});return s[0];}
    private void check(String label,boolean pass){
        if(!pass)throw new AssertionError(label);
        results.add("PASS | "+label);Bundle b=new Bundle();b.putString("stream","PASS | "+label+"\n");sendStatus(0,b);
    }
    private void shot(String name)throws Exception{
        hideKeyboard();pause(200);Bitmap b=getUiAutomation().takeScreenshot();
        if(b==null)throw new IOException("Screenshot unavailable");
        File dir=new File(getTargetContext().getExternalFilesDir(null),"evidence");dir.mkdirs();
        try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}
    }
    private void save(String name,String data)throws Exception{
        File dir=new File(getTargetContext().getExternalFilesDir(null),"evidence");dir.mkdirs();
        try(FileOutputStream out=new FileOutputStream(new File(dir,name))){out.write(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    }
    private Activity waitActivity(String name){
        for(int i=0;i<100;i++){if(active!=null&&active.getClass().getSimpleName().equals(name)){settle();return active;}pause(100);}
        throw new AssertionError("Activity not resumed: "+name);
    }
    private void rotate(boolean landscape){
        Activity old=active;
        ui(()->old.setRequestedOrientation(landscape?ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        for(int i=0;i<80;i++){if(active!=old){settle();return;}pause(100);}
        throw new AssertionError("Rotation did not recreate Activity");
    }
    private void back(){sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);settle();}
    private AccessibilityNodeInfo findNode(String id,String value){
        AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();if(root==null)return null;
        if(id!=null){List<AccessibilityNodeInfo> list=root.findAccessibilityNodeInfosByViewId(id);if(!list.isEmpty())return list.get(0);}
        if(value!=null){List<AccessibilityNodeInfo> list=root.findAccessibilityNodeInfosByText(value);for(AccessibilityNodeInfo node:list)if(value.equals(String.valueOf(node.getText()))||value.equals(String.valueOf(node.getContentDescription())))return node;}
        return null;
    }
    private void systemClick(String id,String text){
        for(int i=0;i<60;i++){AccessibilityNodeInfo node=findNode(id,text);if(node!=null){
            while(node!=null&&!node.isClickable())node=node.getParent();
            if(node!=null&&node.performAction(AccessibilityNodeInfo.ACTION_CLICK)){settle();return;}
        }pause(150);}
        throw new AssertionError("System control not found: "+id+" / "+text);
    }
    private void shell(String command)throws Exception{
        try(ParcelFileDescriptor fd=getUiAutomation().executeShellCommand(command);FileInputStream in=new FileInputStream(fd.getFileDescriptor())){byte[] b=new byte[2048];while(in.read(b)!=-1){}}
        settle();
    }
    private void execute()throws Exception{

        click(R.id.btnXacNhan);check("A6-T01: empty name rejected",!error(R.id.edtHoTen).isEmpty());show(R.id.edtHoTen);shot("01_loi_ten");
        edit(R.id.edtHoTen,"Võ Văn Quốc Bảo");edit(R.id.edtMssv,"123");click(R.id.btnXacNhan);
        check("A6-T02: incorrect student id rejected",error(R.id.edtMssv).contains("10 chữ số"));show(R.id.edtMssv);shot("02_loi_mssv");
        edit(R.id.edtMssv,"2201234567");click(R.id.btnXacNhan);
        check("A6-T03: no training selection keeps MainActivity",active instanceof MainActivity&&((RadioGroup)view(R.id.rgHeDaoTao)).getCheckedRadioButtonId()==-1);shot("03_loi_he");
        click(R.id.rbChinhQuy);click(R.id.btnXacNhan);
        check("A6-T04: no session selected keeps form",active instanceof MainActivity&&!((CheckBox)view(R.id.cbSang)).isChecked());shot("04_loi_buoi");
        click(R.id.cbSang);click(R.id.cbToi);click(R.id.tgUuTien);
        check("A6-NC2: live session count = 2",text(R.id.tvSoBuoi).equals("Đã chọn: 2 buổi"));shot("NC2_dem_buoi");
        click(R.id.btnXacNhan);waitActivity("ConfirmActivity");String summary=text(R.id.tvTomTat);
        check("A6-T05: all 7 summary lines correct",summary.split("\n").length==7&&summary.contains("2201234567")&&summary.contains("Sáng, Tối")&&summary.contains("ưu tiên: Bật"));shot("05_xac_nhan");
        back();waitActivity("MainActivity");check("A6-T08: Back keeps original form",text(R.id.edtHoTen).equals("Võ Văn Quốc Bảo")&&((CheckBox)view(R.id.cbToi)).isChecked());
        rotate(true);check("A6-T07: text and controls survive rotation",text(R.id.edtMssv).equals("2201234567")&&((CheckBox)view(R.id.cbToi)).isChecked()&&((RadioGroup)view(R.id.rgHeDaoTao)).getCheckedRadioButtonId()==R.id.rbChinhQuy&&text(R.id.tvSoBuoi).contains("2"));shot("07_xoay");rotate(false);
        click(R.id.btnLamLai);check("A6-T06: reset clears every control and errors",text(R.id.edtHoTen).isEmpty()&&text(R.id.edtMssv).isEmpty()&&((RadioGroup)view(R.id.rgHeDaoTao)).getCheckedRadioButtonId()==-1&&!((CheckBox)view(R.id.cbSang)).isChecked()&&!((ToggleButton)view(R.id.tgUuTien)).isChecked()&&((android.widget.CompoundButton)view(R.id.swThongBao)).isChecked()&&text(R.id.tvSoBuoi).contains("0")&&error(R.id.edtMssv).isEmpty());shot("06_lam_lai");
        show(R.id.spKhoa);ui(()->((Spinner)view(R.id.spKhoa)).setSelection(1));settle();
        check("A6-NC1: Economics faculty changes course list",((Spinner)view(R.id.spMonHoc)).getSelectedItem().toString().equals("Kinh tế vi mô"));shot("NC1_spinner_phu_thuoc");
        ui(()->((Spinner)view(R.id.spMonHoc)).setSelection(2));rotate(true);
        check("A6-extra: dependent Spinner choice survives rotation",((Spinner)view(R.id.spKhoa)).getSelectedItemPosition()==1&&((Spinner)view(R.id.spMonHoc)).getSelectedItemPosition()==2);rotate(false);
        edit(R.id.edtHoTen,"Bao");edit(R.id.edtMssv,"231A290036");click(R.id.btnXacNhan);
        check("A6-extra: ten-character alphanumeric id is rejected",error(R.id.edtMssv).contains("10 chữ số"));

    }
}
