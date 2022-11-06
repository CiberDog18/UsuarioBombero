package com.example.usuario.providers;


import com.example.usuario.models.Bombero;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class ConductorProvider {

    DatabaseReference mDatabase;

    public ConductorProvider(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("bombero");
    }

    public void createToken(final String idUser) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Map<String, Object> map = new HashMap<>();
                map.put("token", token);
                mDatabase.child(idUser).updateChildren(map);
            }
        });
    }

    public Task<Void> create(Bombero bombero){
        return mDatabase.child(bombero.getId()).setValue(bombero);
    }

    public DatabaseReference getDriver(String idDriver) {
        return mDatabase.child(idDriver);
    }

    public Task<Void> update(Bombero bombero) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", bombero.getName());
        map.put("cedula", bombero.getCedula());
        map.put("apellido", bombero.getApe());
        map.put("direccion", bombero.getAddress());
        map.put("email", bombero.getEmail());
        map.put("sexo", bombero.getSexo());
        map.put("image", bombero.getImage());
        map.put("Nacimiento", bombero.getDateborn());
        map.put("contrato", bombero.getContrato());
        return mDatabase.child(bombero.getId()).updateChildren(map);
    }

    public Task<Void> updateOnline(Bombero bombero, boolean status) {
        Map<String, Object> map = new HashMap<>();
        map.put("online", status);
        return mDatabase.child(bombero.getId()).updateChildren(map);
    }
}
