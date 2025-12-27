package aa.tulybaev.client.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {

    private boolean left, right;
    private boolean jumpPressed;
    private boolean shootPressed;


    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_W -> jumpPressed = true;
            case KeyEvent.VK_SPACE -> shootPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_D -> right = false;
        }
    }

    public boolean consumeJump() {
        if (jumpPressed) {
            jumpPressed = false;
            return true;
        }
        return false;
    }

    public boolean consumeShoot() {
        if (shootPressed) {
            shootPressed = false;
            return true;
        }
        return false;
    }

    public float getDx() {
        if (left && !right) return -1f;
        if (right && !left) return 1f;
        return 0f;
    }


    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
    }

    public void setJumpPressed(boolean jumpPressed) {
        this.jumpPressed = jumpPressed;
    }

    public boolean isShootPressed() {
        return shootPressed;
    }

    public void setShootPressed(boolean shootPressed) {
        this.shootPressed = shootPressed;
    }
}
