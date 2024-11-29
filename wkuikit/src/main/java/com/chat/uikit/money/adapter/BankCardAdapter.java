package com.chat.uikit.money.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.uikit.R;
import com.chat.uikit.money.entity.BankCard;

public class BankCardAdapter extends BaseQuickAdapter<BankCard, BaseViewHolder> {

    public BankCardAdapter() {
        super(R.layout.item_bank_card);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, BankCard card) {
        holder.setText(R.id.bankNameTv, card.bank_name)
                .setText(R.id.bankNumberTv, formatBankNumber(card.bank_number));
    }

    private String formatBankNumber(String number) {
        if (number == null || number.length() < 4) return number;
        return "**** **** **** " + number.substring(number.length() - 4);
    }
} 