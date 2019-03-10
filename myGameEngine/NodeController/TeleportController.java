package myGameEngine.NodeController;

import ray.rage.scene.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;

public class TeleportController extends AbstractController {

    SceneNode target;

    public TeleportController(SceneNode t){
        target = t;
    }

    @Override
    protected void updateImpl(float v) {
        for (Node n : super.controlledNodesList)
        { Vector3 curLocal = Vector3f.createFrom(target.getLocalPosition().x() - .5f, target.getLocalPosition().y(), target.getLocalPosition().z() - .5f);
            n.setLocalScale(.1f,.1f,.1f);
            n.setLocalPosition(curLocal);
        }
    }
}
