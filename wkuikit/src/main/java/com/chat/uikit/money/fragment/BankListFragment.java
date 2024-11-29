package com.chat.uikit.money.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.base.config.WKConfig;
import com.chat.base.net.HttpRequest;
import com.chat.base.net.UserService;
import com.chat.uikit.R;
import com.chat.uikit.money.adapter.BankCardAdapter;
import com.chat.uikit.money.entity.BankCard;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankListFragment extends Fragment {
    private RecyclerView recyclerView;
    private BankCardAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bank_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        adapter = new BankCardAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        loadBankCards();
    }

    private void loadBankCards() {
        String uid = WKConfig.getInstance().getUid();
        Map<String, String> params = new HashMap<>();
        params.put("uid", uid);
        
        HttpRequest.getInstance().get("http://152.42.170.13:8787/V2/getBank", params, 
            new UserService.IHttpRequestCallback() {
                @Override
                public void onSuccess(String data) {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        JSONArray list = jsonObject.optJSONArray("list");
                        List<BankCard> bankCards = new ArrayList<>();
                        if (list != null) {
                            for (int i = 0; i < list.length(); i++) {
                                JSONObject item = list.optJSONObject(i);
                                BankCard card = new Gson().fromJson(item.toString(), BankCard.class);
                                bankCards.add(card);
                            }
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> adapter.setList(bankCards));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(int code, String msg) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
                    }
                }
            });
    }
} 