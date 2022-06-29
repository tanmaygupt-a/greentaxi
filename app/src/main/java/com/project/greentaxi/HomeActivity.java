package com.project.greentaxi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.greentaxi.model.DriverInfoModel;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public static final int LOGIN_REQUEST_CODE = 7191;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private TextView tv_welcome;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference driverInfoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Checking for LogIN validity
        checkForLogin();




    }

    private void initWidgets(){
        //

    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    private void checkUserFromFirebase(){
        driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Toast.makeText(HomeActivity.this, "User Already Registered!", Toast.LENGTH_SHORT).show();
                    DriverInfoModel driverInfoModel = snapshot.getValue(DriverInfoModel.class);
                    goToDriverHomeActivity(driverInfoModel);

                }else {
                    showRegisterLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void goToDriverHomeActivity(DriverInfoModel driverInfoModel) {
        com.project.greentaxi.Common.currentUser = driverInfoModel;
        startActivity(new Intent(HomeActivity.this, com.project.greentaxi.DriverHomeActivity.class));
        finish();
    }

    private void showRegisterLayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.DialogBuilder);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        EditText edt_first_name = (EditText) itemView.findViewById(R.id.et_first_name);
        EditText edt_last_name = (EditText) itemView.findViewById(R.id.et_last_name);
        EditText edt_phone_number = (EditText) itemView.findViewById(R.id.et_register_phone_number);

        Button btn_continue = (Button) itemView.findViewById(R.id.btn_register_submit);

        // Set Data
        if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null &&
                !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())){
                edt_phone_number.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        }

        // Set View
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(edt_first_name.getText().toString())){
                    edt_first_name.setError("Enter First Name");
                }else if(TextUtils.isEmpty(edt_last_name.getText().toString())){
                    edt_last_name.setError("Enter Last Name");
                }else if(TextUtils.isEmpty(edt_phone_number.getText().toString())){
                    edt_phone_number.setError("Enter Phone Number");
                }else {
                    DriverInfoModel model = new DriverInfoModel();
                    model.setFirstName(edt_first_name.getText().toString());
                    model.setLastName(edt_last_name.getText().toString());
                    model.setPhoneNumber(edt_phone_number.getText().toString());
                    model.setRating(0.0);

                    driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(model)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialog.dismiss();
                                    Toast.makeText(HomeActivity.this, "Register Successfully!", Toast.LENGTH_SHORT).show();
                                    goToDriverHomeActivity(model);
                                }
                            });
                }
            }
        });
    }

    private void checkForLogin() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        driverInfoRef = firebaseDatabase.getReference(com.project.greentaxi.Common.DRIVER_INFO_REFERENCE);
        firebaseAuth = FirebaseAuth.getInstance();
        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user != null) {
                Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                initWidgets();
                checkUserFromFirebase();
            }else{
                Intent loginIntent = new Intent(HomeActivity.this, com.project.greentaxi.LoginActivity.class);
                startActivity(loginIntent);

            }
        };

    }
}