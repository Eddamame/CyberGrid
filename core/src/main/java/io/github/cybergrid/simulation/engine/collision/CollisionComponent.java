package io.github.cybergrid.simulation.engine.collision;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CollisionComponent {
    private float width;
    private float height;
    
    // Physics properties
    private boolean solid;
    private boolean isStatic;
    private float bounceForce; 
    private float displaceForce; 

    public CollisionComponent(float width, float height, boolean solid, boolean isStatic, float bounceForce, float displaceForce) {
        this.width = width;
        this.height = height;
        this.solid = solid;
        this.isStatic = isStatic;
        this.bounceForce = bounceForce;
        this.displaceForce = displaceForce;
    }

    public Rectangle getBounds(Vector2 position) {
        return new Rectangle(position.x - (width / 2), position.y - (height / 2), width, height);
    }

    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isSolid() { return solid; }
    public boolean isStatic() { return isStatic; }
    public float getBounceForce() { return bounceForce; }
    public float getDisplaceForce() { return displaceForce; }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
