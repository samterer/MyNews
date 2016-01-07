package com.hzpd.ui.fragments.vote;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hzpd.hflt.R;


public class VoteResultDialog extends Dialog implements View.OnClickListener {
    private LinearLayout vote_close;
    private TextView vote_dia_tv1;
    private Button vote_dia_bu2;
    private String content;
    private String butext;
    private IVoteresultClick click;

    public VoteResultDialog(Context context, int theme
            , String content, String buText
            , IVoteresultClick click) {
        super(context, theme);
        this.content = content;
        this.butext = buText;
        this.click = click;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.voteresultdialog_layout, null);
        setContentView(view);
        vote_close = (LinearLayout) view.findViewById(R.id.vote_close);
        vote_close.setOnClickListener(this);
        vote_dia_tv1 = (TextView) view.findViewById(R.id.vote_dia_tv1);
        vote_dia_bu2 = (Button) view.findViewById(R.id.vote_dia_bu2);
        vote_dia_bu2.setText(butext);
        vote_dia_tv1.setText(content);

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vote_close:
                dismiss();
                break;
            case R.id.vote_dia_bu2: {
                this.dismiss();
                if (null != click) {
                    click.onclick();
                }
            }
            break;
        }
    }
}


