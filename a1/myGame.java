package a1;
import myGameEngine.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javafx.scene.Scene;
import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.input.*;
import ray.input.action.*;
import java.util.Random;

public class myGame extends VariableFrameRateGame {

    // to minimize variable allocation in update()
    GL4RenderSystem rs;
    float elapsTime = 0.0f;
    String elapsTimeStr, counterStr, dispStr;
    int elapsTimeSec, counter = 0, maxPlanets = 5;
    GenericInputManager im;

    public myGame() {
        super();
        System.out.println("press T to render triangles");
        System.out.println("press L to render lines");
        System.out.println("press P to render points");
        System.out.println("press C to increment counter");
    }

    public static void main(String[] args) {
        Game game = new myGame();
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }

    @Override
    protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
        rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
    }

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        SceneNode rootNode = sm.getRootSceneNode();
        Camera camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);
        camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
        camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
        camera.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 0.0f));

        SceneNode cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
        cameraNode.attachObject(camera);
    }

    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {

        // This will call a function that will create the inputs for the game.
        setupInputs();

        /*========= Objects to set up planets ==================================================== */
        SceneNode[] planetAmount = new SceneNode[maxPlanets];
        Entity[] planetE = new Entity[maxPlanets];
        int maxDistance = 10;
        int minDistance = 2;
        /*=======================================================================*/

        /*========= Objects to set up changing textures ==================================================== */
        String[] textureFiles = {"blue.jpeg","chain-fence.jpeg", "earth-day.jpeg","earth-night.jpeg","hexagons.jpeg", "moon.jpeg", "red.jpeg"};
        TextureManager tm = eng.getTextureManager();
        RenderSystem rs = sm.getRenderSystem();
        TextureState state;
        /*=======================================================================*/

        /*========= DOLPHIN and DOLPHIN CAMERA NODE ==================================================== */
        Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);

        SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode("dolphinENode");
        dolphinN.moveBackward(2.0f);
        dolphinN.attachObject(dolphinE);

        SceneNode dolphineCN = dolphinN.createChildSceneNode("dolphinCameraNode");
        /*=======================================================================*/

        /*========= PLANETS ==================================================== */
        for (int i = 0; i < maxPlanets; i++){
            planetE[i] = sm.createEntity("myPlanet" + i, "earth.obj");
            planetE[i].setPrimitive(Primitive.TRIANGLES);
        }

        for (int i = 0; i < maxPlanets; i++){
            float scalePlanetNum = new Random().nextFloat();
            planetAmount[i] = sm.getRootSceneNode().createChildSceneNode(planetE[i].getName() + "Node");
            planetAmount[i].moveBackward((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].moveLeft((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].moveRight((float)new Random().nextInt((maxDistance - minDistance) + 1) + minDistance);
            planetAmount[i].scale(scalePlanetNum,scalePlanetNum,scalePlanetNum);
            planetAmount[i].attachObject(planetE[i]);
        }
        /*=======================================================================*/

        /*======== LIGHTING ====================================================*/
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));

        Light plight = sm.createLight("testLamp1", Light.Type.POINT);
        plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
        plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(5f);

        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        /*=======================================================================*/

        /*======== ROTATION and Texture Set ====================================================*/
        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .02f);
        rc.addNode(dolphinN);
        for (int i = 0; i < maxPlanets; i++){
            state = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
            rc.addNode(planetAmount[i]);
            state.setTexture(tm.getAssetByPath(textureFiles[new Random().nextInt(textureFiles.length)]));
            planetE[i].setRenderState(state);
        }
        sm.addController(rc);
        /*=======================================================================*/
    }


    protected void setupInputs(){
        // build some action objects for doing things in response to user input
        QuitGameAction quitGameAction = new QuitGameAction(this);
        IncrementCounterAction incrementCounterAction = new IncrementCounterAction(this);
        CameraChangeView cameraChangeView = new CameraChangeView(this);
        CameraMoveFoward cameraMoveFoward = new CameraMoveFoward(this);

        // Creates and sets up inputs.
        im = new GenericInputManager();
        String kbName = im.getKeyboardName();
        System.out.println(kbName);
        try{
            String gpName = im.getFirstGamepadName();
            System.out.println(gpName);

            im.associateAction(gpName,
                    net.java.games.input.Component.Identifier.Button._9,
                    quitGameAction,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(gpName,
                    net.java.games.input.Component.Identifier.Button._3,
                    incrementCounterAction,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(gpName,
                    net.java.games.input.Component.Identifier.Button._1,
                    cameraChangeView,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
            im.associateAction(gpName,
                    net.java.games.input.Component.Identifier.Button._2,
                    cameraMoveFoward,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }
        catch (Exception e){
            System.out.println("No Controller Detected");
        }

        // attach the action objects to keyboard and gamepad components
        im.associateAction(kbName,
                net.java.games.input.Component.Identifier.Key.ESCAPE,
                quitGameAction,
                InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        im.associateAction(kbName,
                net.java.games.input.Component.Identifier.Key.C,
                incrementCounterAction,
                InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        im.associateAction(kbName,
                net.java.games.input.Component.Identifier.Key.V,
                cameraChangeView,
                InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    }


    @Override
    protected void update(Engine engine) {
        // build and set HUD
        rs = (GL4RenderSystem) engine.getRenderSystem();
        elapsTime += engine.getElapsedTimeMillis();
        elapsTimeSec = Math.round(elapsTime/1000.0f);
        elapsTimeStr = Integer.toString(elapsTimeSec);
        counterStr = Integer.toString(counter);
        im.update(elapsTime);
        dispStr = "Time = " + elapsTimeStr + "   Keyboard hits = " + counterStr;
        rs.setHUD(dispStr, 15, 15);
    }

    public void cameraOffDolphin(Camera camera){
        SceneNode mainCamera = getEngine().getSceneManager().getSceneNode("MainCameraNode");
        SceneNode dolphinCamera = getEngine().getSceneManager().getSceneNode("dolphinCameraNode");
        mainCamera.attachObject(camera);
        camera.setPo((Vector3f)Vector3f.createFrom(dolphinCamera.getLocalPosition().x() + 0.2f, dolphinCamera.getLocalPosition().y() - 0.5f, dolphinCamera.getLocalPosition().x() + 0.5f));
    }

    public void cameraOnDolphin(Camera camera){
        SceneNode dolphinCamera = getEngine().getSceneManager().getSceneNode("dolphinCameraNode");
        dolphinCamera.setLocalPosition(Vector3f.createFrom(0.0f, 0.5f, -0.5f));
        dolphinCamera.attachObject(camera);
    }

    public void incrementCounter() {
        counter++;
    }

//    @Override
//    public void keyPressed(KeyEvent e) {
//        Entity dolphin = getEngine().getSceneManager().getEntity("myDolphin");
//        switch (e.getKeyCode()) {
//            case KeyEvent.VK_L:
//                dolphin.setPrimitive(Primitive.LINES);
//                break;
//            case KeyEvent.VK_T:
//                dolphin.setPrimitive(Primitive.TRIANGLES);
//                break;
//            case KeyEvent.VK_P:
//                dolphin.setPrimitive(Primitive.POINTS);
//                break;
//            case KeyEvent.VK_C:
//                counter++;
//                break;
//            case KeyEvent.VK_X:
//                break;
//        }
//        super.keyPressed(e);
//    }

}
