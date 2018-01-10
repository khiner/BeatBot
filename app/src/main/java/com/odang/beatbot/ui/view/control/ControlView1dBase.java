package com.odang.beatbot.ui.view.control;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.listener.ParamListener;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.view.View;

public abstract class ControlView1dBase extends ControlViewBase implements ParamListener {

    protected Param param;

    protected abstract float posToLevel(Pointer pos);

    public ControlView1dBase(View view) {
        super(view);
    }

    public ControlView1dBase(View view, RenderGroup renderGroup) {
        super(view, renderGroup);
    }

    public void setParam(Param param) {
        if (null == param)
            return;
        if (null != this.param) {
            this.param.removeListener(this);
        }
        this.param = param;
        this.param.addListener(this);
        onParamChange(param);
    }

    public float getLevel() {
        return param.level;
    }

    public Param getParam() {
        return param;
    }

    @Override
    public void handleActionDown(int id, Pointer pos) {
        super.handleActionDown(id, pos);
        param.setLevel(posToLevel(pos));
    }

    @Override
    public void handleActionMove(int id, Pointer pos) {
        super.handleActionMove(id, pos);
        param.setLevel(posToLevel(pos));
    }

    @Override
    public abstract void onParamChange(Param param);
}
