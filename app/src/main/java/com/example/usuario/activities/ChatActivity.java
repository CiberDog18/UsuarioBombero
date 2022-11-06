package com.example.usuario.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.R;
import com.example.usuario.adapters.MessagesAdapter;
import com.example.usuario.models.Chat;
import com.example.usuario.models.Cliente;
import com.example.usuario.models.FCMBody;
import com.example.usuario.models.FCMResponse;
import com.example.usuario.models.Message;
import com.example.usuario.providers.ChatsProvider;
import com.example.usuario.models.Bombero;
import com.example.usuario.providers.AuthProvider;
import com.example.usuario.providers.ClientProvider;
import com.example.usuario.providers.ConductorProvider;
import com.example.usuario.providers.FilesProvider;
import com.example.usuario.providers.MessagesProvider;
import com.example.usuario.providers.NotificationProvider;
import com.example.usuario.providers.TokenProvider;
import com.example.usuario.utils.AppBackgroundHelper;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {


    String mIdDriver;
    String mExtraidChat;

    ConductorProvider mDriverProvider;
    ClientProvider mClientProvider;
    AuthProvider mAuthProvider;
    ChatsProvider mChatsProvider;
    MessagesProvider mMessagesProvider;
    FilesProvider mFilesProvider;
    NotificationProvider mNotificationProvider;
    TokenProvider mTokenProvider;

    Timer mTimer;

    EditText mEditTextMessage;

    ImageView mImageViewBack;
    ImageView mImageViewSend;
    ImageView mImageViewSelectPictures;
    ImageView mImageViewSelectFile;


    TextView mTextViewUsername;
    TextView mTextViewOnline;
    CircleImageView mCircleImageUser;



    MessagesAdapter mAdapter;
    RecyclerView mRecyclerViewMessages;
    LinearLayoutManager mLinearLayoutManager;

    ListenerRegistration mListenerChat;


    Bombero mBomberoReceiver;
    Cliente mMyClient;

    Options mOptions;
    ArrayList<String> mReturnValues = new ArrayList<>();

    final int ACTION_FILE = 2;
    ArrayList<Uri> mFileList;

    Chat mChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuthProvider = new AuthProvider();
        mDriverProvider = new ConductorProvider();
        mClientProvider = new ClientProvider();
        mChatsProvider = new ChatsProvider();
        mMessagesProvider = new MessagesProvider();
        mFilesProvider = new FilesProvider();
        mTokenProvider = new TokenProvider();
        mNotificationProvider = new NotificationProvider();

        mIdDriver = getIntent().getStringExtra("idDriver");
        mExtraidChat = getIntent().getStringExtra("idChat");


        mEditTextMessage = findViewById(R.id.editTextMessage);
        mImageViewSend = findViewById(R.id.imageViewSend);
        mImageViewSelectPictures = findViewById(R.id.imageViewSelectPictures);
        mImageViewSelectFile = findViewById(R.id.imageViewSelectFiles);

        mRecyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        mLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessages.setLayoutManager(mLinearLayoutManager);

        mOptions = Options.init()
                .setRequestCode(100)
                .setCount(5)
                .setFrontfacing(false)
                .setPreSelectedUrls(mReturnValues)
                .setExcludeVideos(true)
                .setVideoDurationLimitinSeconds(0)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
                .setPath("/pix/images");

        showChatToolbar(R.layout.chat_toolbar);

        getUserReciverInfo();
        getMyUserInfo();

        checkIfExistChat();

        setWriting();


        mImageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMessage();

            }
        });

        mImageViewSelectPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPix();
            }
        });

        mImageViewSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFiles();
            }
        });


    }

    private void selectFiles() {
        String[] mimeTypes =
                {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",
                        "application/zip"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"), ACTION_FILE);
    }

    private void setWriting() {
        mEditTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            // ESTA ESCRIBIENDO
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mTimer != null) {
                    if (mExtraidChat != null) {
                        mChatsProvider.updateWriting(mExtraidChat, mAuthProvider.getId());
                        mTimer.cancel();
                    }
                }

            }
            // SI EL USUARIO DEJO DE ESCRIBIR
            @Override
            public void afterTextChanged(Editable editable) {
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mExtraidChat != null) {
                            mChatsProvider.updateWriting(mExtraidChat, "");
                        }
                    }
                }, 2000);


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppBackgroundHelper.online(ChatActivity.this, true);
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
        AppBackgroundHelper.online(ChatActivity.this, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListenerChat != null) {
            mListenerChat.remove();
        }
    }

    private void startPix() {
        Pix.start(ChatActivity.this, mOptions);
    }



    private void createMessage() {

        String textMessage = mEditTextMessage.getText().toString();
        if (!textMessage.equals("")) {
            Message message = new Message();
            message.setIdChat(mExtraidChat);
            message.setIdSender(mAuthProvider.getId());
            message.setIdReceiver(mIdDriver);
            message.setMessage(textMessage);
            message.setStatus("ENVIADO");
            message.setType("texto");
            message.setTimestamp(new Date().getTime());

            mMessagesProvider.create(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    mEditTextMessage.setText("");
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    mChatsProvider.updateNumberMessages(mExtraidChat);
                    getLastMessages(message);
                    //Toast.makeText(ChatActivity.this, "El mensaje se creo correctamente", Toast.LENGTH_SHORT).show();


                }
            });
        }
        else{
            Toast.makeText(this, "Ingresa el mensaje", Toast.LENGTH_SHORT).show();

        }
    }

    private void getLastMessages(final Message message) {
        mMessagesProvider.getLastMessagesByChatAndSender(mExtraidChat, mAuthProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (querySnapshot != null) {
                    ArrayList<Message> messages = new ArrayList<>();

                    for(DocumentSnapshot document: querySnapshot.getDocuments()) {
                        Message m = document.toObject(Message.class);
                        messages.add(m);
                    }

                    if (messages.size() == 0) {
                        messages.add(message);
                    }
                    Collections.reverse(messages);
                      sendNotification(messages);
                }
            }
        });
    }

    private void sendNotification(ArrayList<Message> messages) {
        Map<String, String> data = new HashMap<>();
        data.put("title", "MENSAJE");
        data.put("body", "texto mensaje");
       // data.put("idNotification", String.valueOf(mChat.getIdNotification()));
        data.put("usernameReceiver", mBomberoReceiver.getName());
        data.put("usernameSender", mMyClient.getName());
        data.put("imageReceiver", mBomberoReceiver.getImage());

        Gson gson = new Gson();
        String messagesJSON = gson.toJson(messages);
        data.put("messagesJSON", messagesJSON);

        mNotificationProvider.send(ChatActivity.this, mBomberoReceiver.getToken(), data);



    }

    private void checkIfExistChat() {
        mChatsProvider.getChatByUser1AndUser2(mIdDriver, mAuthProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    if (queryDocumentSnapshots.size() == 0) {
                        createChat();
                        getChatInfo();
                    }
                    else {
                        mExtraidChat = queryDocumentSnapshots.getDocuments().get(0).getId();
                        getMessagesByChat();
                        updateStatus();
                        getChatInfo();


                    }
                }

            }
        });

    }

    private void getChatInfo() {
        mListenerChat = mChatsProvider.getChatById(mExtraidChat).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        mChat = documentSnapshot.toObject(Chat.class);
                        if (mChat.getWriting() != null) {
                            if (!mChat.getWriting().equals("")) {
                                if (!mChat.getWriting().equals(mAuthProvider.getId())) {
                                    mTextViewOnline.setText("Escribiendo...");
                                }
                                else if (mBomberoReceiver != null) {
                                    if (mBomberoReceiver.isOnline()) {
                                        mTextViewOnline.setText("En linea");
                                    }
                                    else {
                                        mTextViewOnline.setText("...");
                                    }
                                }
                                else {
                                    mTextViewOnline.setText("");
                                }
                            }
                            else if (mBomberoReceiver != null) {
                                if (mBomberoReceiver.isOnline()) {
                                    mTextViewOnline.setText("En linea");
                                }
                                else {
                                    mTextViewOnline.setText("...");
                                }
                            }
                            else {
                                mTextViewOnline.setText("");
                            }


                        }
                    }
                }
            }
        });
    }

    private void updateStatus() {
        mMessagesProvider.getMessagesNotRead(mExtraidChat).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot document: queryDocumentSnapshots.getDocuments()) {
                    Message message = document.toObject(Message.class);
                    if (!message.getIdSender().equals(mAuthProvider.getId())) {
                        mMessagesProvider.updateStatus(message.getId(), "VISTO");
                    }
                }



            }
        });
    }

    private void getMessagesByChat() {

        Query query = mMessagesProvider.getMessagesByChat(mExtraidChat);

        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();

        mAdapter = new MessagesAdapter(options, ChatActivity.this);
        mRecyclerViewMessages.setAdapter(mAdapter);
        mAdapter.startListening();

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateStatus();
                int numberMessage = mAdapter.getItemCount();
                int lastMessagePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastMessagePosition == -1 || (positionStart >= (numberMessage - 1) && lastMessagePosition == (positionStart -1))) {
                    mRecyclerViewMessages.scrollToPosition(positionStart);
                }

            }
        });

    }

    private void createChat() {
      //  Random random = new Random();
        //int n = random.nextInt(100000);

        mChat = new Chat();
        mChat.setId(mAuthProvider.getId() + mIdDriver);
        mChat.setTimestamp(new Date().getTime());
        mChat.setNumberMessages(0);
        mChat.setWriting("");
       // mChat.setIdNotification(n);

        ArrayList<String> ids = new ArrayList<>();
        ids.add(mAuthProvider.getId());
        ids.add(mIdDriver);

        mChat.setIds(ids);

        mExtraidChat = mChat.getId();

        mChatsProvider.create(mChat).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                getMessagesByChat();


            }
        });

    }


    private void getMyUserInfo() {
        mClientProvider.getClient(mAuthProvider.getId()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mMyClient = dataSnapshot.getValue(Cliente.class);
                }

            }
        });
    }

    private void getUserReciverInfo() {
        mDriverProvider.getDriver(mIdDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mBomberoReceiver = dataSnapshot.getValue(Bombero.class);
                    mTextViewUsername.setText(mBomberoReceiver.getName());
                    if (mBomberoReceiver.getImage() != null){
                        if (!mBomberoReceiver.getImage().equals("")){
                            Picasso.with(ChatActivity.this).load(mBomberoReceiver.getImage()).into(mCircleImageUser);
                        }
                    }
                    if (mBomberoReceiver.isOnline()) {
                        mTextViewOnline.setText("En linea");
                    }
                    else{
                        mTextViewOnline.setText("...");
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showChatToolbar (int resource) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(resource, null);
        actionBar.setCustomView(view);


        mTextViewUsername = view.findViewById(R.id.textViewUsername);
        mCircleImageUser = view.findViewById(R.id.circleImageUser);
        mImageViewBack = view.findViewById(R.id.imageViewBack);
        mTextViewOnline = view.findViewById(R.id.textViewOnline);

        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            mReturnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            Intent intent = new Intent(ChatActivity.this, ConfirmImageSendActivity.class);
            intent.putExtra("data", mReturnValues);
            intent.putExtra("idChat", mExtraidChat);
            intent.putExtra("idReceiver", mIdDriver);
            startActivity(intent);

        }

        if (requestCode == ACTION_FILE && resultCode == RESULT_OK) {
            mFileList = new ArrayList<>();

            ClipData clipData = data.getClipData();

            // SELECCIONO UN SOLO ARCHIVO
            if (clipData == null) {
                Uri uri = data.getData();
                mFileList.add(uri);
            }
            // SELECCIONO VARIOS ARCHIVOS
            else {
                int count = clipData.getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    mFileList.add(uri);
                }
            }


            mFilesProvider.saveFiles(ChatActivity.this, mFileList, mExtraidChat, mIdDriver);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(ChatActivity.this, mOptions);
                } else {
                    Toast.makeText(ChatActivity.this, "Por favor concede los permisos para acceder a la camara", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


}