package myGameEngine.Camera;
import a1.myGame;
import net.java.games.input.Component;
import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Camera3Pcontroller {
    private Camera camera; //the camera being controlled
    private SceneNode cameraN; //the node the camera is attached to
    private SceneNode target; //the target the camera looks at
    private float cameraAzimuth; //rotation of camera around Y axis
    private float cameraElevation; //elevation of camera above target
    private float radias; //distance between camera and target
    private Vector3 targetPos; //target’s position in the world
    private Vector3 worldUpVec;

    public Camera3Pcontroller(Camera cam, SceneNode camN,
                              SceneNode targ, String controllerName, InputManager im) {
        camera = cam;
        cameraN = camN;
        target = targ;
        cameraAzimuth = 225.0f; // start from BEHIND and ABOVE the target
        cameraElevation = 20.0f; // elevation is in degrees
        radias = 2.0f;
        worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
        setupInput(im, controllerName);
        updateCameraPosition();
    }

    private void setupInput(InputManager im, String cn) {
        Action orbitAAction = new OrbitAroundAction();
        Action orbitAUDction = new OrbitUpDownAction();
        Action OrbitRadiasAction = new RadiasAction();
        im.associateAction(cn,
                Component.Identifier.Axis.RX, orbitAAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(cn,
                Component.Identifier.Axis.RY, orbitAUDction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(cn,
                Component.Identifier.Axis.Z, OrbitRadiasAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
// similar input set up for OrbitRadiasAction, OrbitElevationAction
    }


    public void updateCameraPosition()
    { double theta = Math.toRadians(cameraAzimuth); // rot around target
        double phi = Math.toRadians(cameraElevation); // altitude angle
        double x = radias * Math.cos(phi) * Math.sin(theta);
        double y = radias * Math.sin(phi);
        double z = radias * Math.cos(phi) * Math.cos(theta);
        cameraN.setLocalPosition(Vector3f.createFrom
                ((float)x, (float)y, (float)z).add(target.getWorldPosition()));
        cameraN.lookAt(target, worldUpVec);
    }

    private class OrbitAroundAction extends AbstractInputAction
    {
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;
            if (evt.getValue() < -0.2)
            { rotAmount=-0.2f; }
            else
            { if (evt.getValue() > 0.2)
            { rotAmount=0.2f; }
            else
            { rotAmount=0.0f; }
            }
            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class RadiasAction extends AbstractInputAction
    { // Moves the camera around the target (changes camera azimuth).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float zoomAmount;
            if (evt.getValue() < -0.2)
            { zoomAmount=-0.2f; }
            else
            { if (evt.getValue() > 0.2)
            { zoomAmount=0.2f; }
            else
            { zoomAmount=0.1f; }
            }
            radias += zoomAmount;

            if (radias <= 0){
                radias = .1f;
            }

            updateCameraPosition();
        }
    }

    private class OrbitUpDownAction extends AbstractInputAction
    { // Moves the camera around the target (changes camera azimuth).
        public void performAction(float time, net.java.games.input.Event evt)
        {
            float rotAmount;
            if (evt.getValue() < -0.2)
            { rotAmount=-0.2f; }
            else
            { if (evt.getValue() > 0.2)
            { rotAmount=0.2f; }
            else
            { rotAmount=0.0f; }
            }
            cameraElevation += rotAmount;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }

}