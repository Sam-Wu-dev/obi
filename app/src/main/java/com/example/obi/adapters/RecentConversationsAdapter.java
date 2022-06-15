package com.example.obi.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.obi.databinding.ItemContainerRecentConversionBinding;
import com.example.obi.listeners.ConversionListener;
import com.example.obi.models.User;
import com.example.obi.utilities.UserManager;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {

    private final List<UserManager.ChatRoom> chatRooms;
    private final ConversionListener conversionListener;

    public RecentConversationsAdapter(List<UserManager.ChatRoom> chatRooms, ConversionListener conversionListener) {
        this.chatRooms = chatRooms;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatRooms.get(position));
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(UserManager.ChatRoom chatRoom) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatRoom.getFriendImage()));
            binding.textName.setText(chatRoom.getFriendName());
            binding.textRecentMessage.setText(chatRoom.getLastMessage());
            binding.getRoot().setOnClickListener(view -> {
                User user = new User();
                user.id = chatRoom.getFriendId();
                user.name = chatRoom.getFriendName();
                user.image= chatRoom.getFriendImage();
                conversionListener.onConversionClicked(user);
            });
        }
    }

    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
