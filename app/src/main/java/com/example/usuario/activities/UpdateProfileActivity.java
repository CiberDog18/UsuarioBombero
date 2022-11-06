package com.example.usuario.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.R;
import com.example.usuario.models.Cliente;
import com.example.usuario.providers.AuthProvider;
import com.example.usuario.providers.ClientProvider;
import com.example.usuario.providers.ImagesProvider;
import com.example.usuario.utils.FileUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class UpdateProfileActivity extends AppCompatActivity {

    private ImageView mImageViewProfile;
    private Button mButtonUpdate;
    private TextView mTextViewEmail2;
    private TextView mTextViewAddress2;
    private CircleImageView mCircleImageBack;
    Spinner mSpinnertCity;

    private ClientProvider mClientProvider;
    private AuthProvider mAuthProvider;
    private ImagesProvider mImageProvider;



    private File mImageFile;
    private String mImage;

    private final int GALLERY_REQUEST = 1;
    private ProgressDialog mProgressDialog;
    private String mName;
    private String mEmail;
    private String mAddress;
    private String ciuidad;

    List<String> municipiosSantander = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mImageViewProfile = findViewById(R.id.imageViewProfile);
        mButtonUpdate = findViewById(R.id.btnUpdateProfile);
        mTextViewEmail2 = findViewById(R.id.textInputEmail2);
        mTextViewAddress2 = findViewById(R.id.textInputDireccion);
        mCircleImageBack = findViewById(R.id.circleImageBack);
        mSpinnertCity = findViewById(R.id.spinnerCity);

        mClientProvider = new ClientProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImagesProvider("user_images");
        mProgressDialog = new ProgressDialog(this);
        listSpinner();
        getClientInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        
        
        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void listSpinner() {
        municipiosSantander.add(0, "Seleccionar municipio");
        municipiosSantander.add("Paramo");
        municipiosSantander.add("Socorro");
        municipiosSantander.add("Valle de San Jose");
        municipiosSantander.add("Curiti");
        municipiosSantander.add("Villa nueva");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, municipiosSantander);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnertCity.setAdapter(arrayAdapter);



    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {
                mImageFile = FileUtil.from(this, data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch(Exception e) {
                Log.d("ERROR", "Mensaje: " +e.getMessage());
            }
        }

    }

    private void getClientInfo() {
        mClientProvider.getClient(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    String address = dataSnapshot.child("direccion").getValue().toString();
                    String image = "";
                    if (dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfileActivity.this).load(image).into(mImageViewProfile);
                    }

                    mTextViewEmail2.setText(email);
                    mTextViewAddress2.setText(address);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateProfile() {
        mEmail = mTextViewEmail2.getText().toString();
        mAddress = mTextViewAddress2.getText().toString();
        ciuidad = mSpinnertCity.getSelectedItem().toString();
        if (!mEmail.equals("") && !mAddress.equals("") && !ciuidad.equals("Seleccionar municipio") && mImageFile != null) {
            mProgressDialog.setMessage("Espere un momento...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();


            saveImage();
        }
        else {
            Toast.makeText(this, "Ingresa la imagen, el nombre y la ciudad", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveImage() {
        mImageProvider.saveImage(UpdateProfileActivity.this, mImageFile, mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Cliente cliente = new Cliente();
                            cliente.setImage(image);
                            cliente.setEmail(mEmail);
                            cliente.setAddress(mAddress);
                            cliente.setCiuidad(ciuidad);
                            cliente.setId(mAuthProvider.getId());
                            mClientProvider.update(cliente).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(UpdateProfileActivity.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else {
                    Toast.makeText(UpdateProfileActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}