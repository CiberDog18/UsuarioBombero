package com.example.usuario.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.usuario.R;
import com.example.usuario.includes.MyToolbar;
import com.example.usuario.models.Cliente;
import com.example.usuario.providers.AuthProvider;
import com.example.usuario.providers.ClientProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dmax.dialog.SpotsDialog;


public class RegisterActivity extends AppCompatActivity {

    AlertDialog mDialog;
    // views
    Button mButtonRegister;
    AuthProvider mAuthProvider;
    ClientProvider mClientProvider;
    Spinner mSpinnertCiudad;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInpuDoc;
    TextInputEditText mTextInputApe;
    TextInputEditText mTextInputAddress;
    TextInputEditText mTextInputEmail;
    Spinner mSpinnerSex;
    private DatePickerDialog mDatePickerDialog;
    private Button mDateButton;



    private List<String> municipiosSantander = new ArrayList<>();
    private List<String> sexos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MyToolbar.show(this, "Registrar Usuario", false);

        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();

        mDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momento").build();

        mTextInputName = findViewById(R.id.textInputNameR);
        mTextInpuDoc = findViewById(R.id.textInpuDoc);
        mTextInputApe = findViewById(R.id.textInputApe);
        mTextInputAddress = findViewById(R.id.textInputAddress);
        mTextInputEmail = findViewById(R.id.textInputEmailR);


        mDateButton = findViewById(R.id.datePickerButton);
        mButtonRegister = findViewById(R.id.btnResgister);
        mSpinnertCiudad = findViewById(R.id.spinnerCiudad);
        mSpinnerSex = findViewById(R.id.spinnerSex);

        initDatePicker();
        mDateButton.setText(getTodaysDate());

        listSpinner();


        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegister();
            }
        });



    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    private String getTodaysDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day, month, year);

                mDateButton.setText(date);

            }
        };
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        mDatePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);

    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    private String makeDateString(int day, int month, int year)
    {
        return getMonthFormat(month) + " " + day + " " + year;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    private String getMonthFormat(int month)
    {
        if(month == 1)
            return "JAN";
        if(month == 2)
            return "FEB";
        if(month == 3)
            return "MAR";
        if(month == 4)
            return "APR";
        if(month == 5)
            return "MAY";
        if(month == 6)
            return "JUN";
        if(month == 7)
            return "JUL";
        if(month == 8)
            return "AUG";
        if(month == 9)
            return "SEP";
        if(month == 10)
            return "OCT";
        if(month == 11)
            return "NOV";
        if(month == 12)
            return "DEC";

        //default should never happen
        return "JAN";
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void openDatePicker(View view)
    {
        mDatePickerDialog.show();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void listSpinner() {

        sexos.add(0, "Sexo");
        sexos.add("Hombre");
        sexos.add("Mujer");

        ArrayAdapter<String> arrayAdapterSex = new ArrayAdapter(this, android.R.layout.simple_list_item_1, sexos);
        arrayAdapterSex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSex.setAdapter(arrayAdapterSex);

        municipiosSantander.add(0, "Seleccionar municipio");
        municipiosSantander.add("Paramo");
        municipiosSantander.add("Socorro");
        municipiosSantander.add("Valle de San Jose");
        municipiosSantander.add("Curiti");
        municipiosSantander.add("Villa nueva");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, municipiosSantander);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnertCiudad.setAdapter(arrayAdapter);
    }

    void clickRegister() {
        final String doc = mTextInpuDoc.getText().toString();
        final String name = mTextInputName.getText().toString();
        final String ape = mTextInputApe.getText().toString();
        final String ciuidad = mSpinnertCiudad.getSelectedItem().toString();
        final String address = mTextInputAddress.getText().toString();
        final String email = mTextInputEmail.getText().toString();
        final String sex = mSpinnerSex.getSelectedItem().toString();
        final String date = mDateButton.getText().toString();


        if (!ciuidad.equals("Seleccionar municipio") && !sex.equals("Sexo")) {

            if (!doc.isEmpty() && !name.isEmpty() && !ape.isEmpty() && !address.isEmpty() && !email.isEmpty() && !sex.isEmpty() && !ciuidad.isEmpty() && !date.isEmpty()) {
                mDialog.show();
                Cliente cliente = new Cliente();
                cliente.setId(mAuthProvider.getId());
                cliente.setCedula(doc);
                cliente.setName(name);
                cliente.setApe(ape);
                cliente.setAddress(address);
                cliente.setEmail(email);
                cliente.setSexo(sex);
                cliente.setCiuidad(ciuidad);
                cliente.setDateborn(date);

                create(cliente);
                //  register(name, email, password);
            }

        }




    }

    void register(final String name, final String email, String password) {
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if (task.isSuccessful()) {
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();


                } else {
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    void create(Cliente cliente) {
        mClientProvider.create(cliente).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(RegisterActivity.this, MapClientActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(RegisterActivity.this, "No se puedo crear el cliente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

/*
    void saveUser(String id, String name, String email) {
        String selectedUser = mPref.getString("user", "");
        User user = new User();
        user.setEmail(email);
        user.setName(name);

        if (selectedUser.equals("conductor")) {

            mDatabase.child("Users").child("Drivers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (selectedUser.equals("cliente")) {
            mDatabase.child("Users").child("Clientes").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }


    }*/

}