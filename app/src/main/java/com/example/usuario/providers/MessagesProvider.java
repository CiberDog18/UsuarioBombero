package com.example.usuario.providers;

import com.example.usuario.models.Message;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class MessagesProvider {

    CollectionReference mCollection;
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public MessagesProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Messages");
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Task<Void> create(Message message) {
        DocumentReference document = mCollection.document();
        message.setId(document.getId());
        return document.set(message);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Query getMessagesByChat(String idChat) {
        return mCollection.whereEqualTo("idChat", idChat).orderBy("timestamp", Query.Direction.ASCENDING);
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Query getLastMessagesByChatAndSender(String idChat, String idSender) {
        return mCollection
                .whereEqualTo("idChat", idChat)
                .whereEqualTo("idSender", idSender)
                .whereEqualTo("status", "ENVIADO")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Task<Void> updateStatus(String idMessage, String status) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        return mCollection.document(idMessage).update(map);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Query getMessagesNotRead(String idChat) {
        return mCollection.whereEqualTo("idChat", idChat).whereEqualTo("status", "ENVIADO");
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Query getLastMessage(String idChat) {
        return mCollection.whereEqualTo("idChat", idChat).orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
