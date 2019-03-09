package myGameEngine;
import a1.*;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class CameraTiltLeftRight extends AbstractInputAction {

    private SceneNode dolphinCamNode;

    public CameraTiltLeftRight(myGame g){
        dolphinCamNode = g.getEngine().getSceneManager().getSceneNode("dolphinENode");
    }

    @Override
    public void performAction(float v, Event event) {

        float degreeAmount = .7f;
        Angle rotAmt = Degreef.createFrom(degreeAmount);

        if(event.getValue() < -0.1 || event.getComponent().getName().equals("Left")){
                dolphinCamNode.yaw(rotAmt);

        }else if (event.getValue() > 0.1 || event.getComponent().getName().equals("Right")){
                dolphinCamNode.yaw(rotAmt);
        }

    }
}
