package katzman.yuval.artdash;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import android.graphics.Bitmap;

public class DrawingView extends View {

    private Path drawPath;
    private Paint drawPaint;
    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Paint> paints = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();
    private ArrayList<Paint> undonePaints = new ArrayList<>();

    private int currentColor = Color.BLACK;
    private float currentStrokeWidth = 10f; // עובי ברירת מחדל
    private boolean isEraserMode = false;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        drawPath = new Path();
        setupPaint();
    }

    private void setupPaint() {
        drawPaint = new Paint();
        drawPaint.setColor(currentColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(currentStrokeWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        if (isEraserMode) {
            drawPaint.setColor(Color.WHITE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < paths.size(); i++) {
            canvas.drawPath(paths.get(i), paints.get(i));
        }
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                undonePaths.clear();
                drawPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                paths.add(new Path(drawPath));
                paints.add(new Paint(drawPaint));
                drawPath.reset();
                break;
        }
        invalidate();
        return true;
    }


    public void setStrokeWidth(float newWidth) {
        currentStrokeWidth = newWidth;
        setupPaint();
    }

    public void setColor(int color) {
        isEraserMode = false;
        this.currentColor = color;
        setupPaint();
    }

    public void setEraserMode(boolean enabled) {
        isEraserMode = enabled;
        setupPaint();
    }

    public void undo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            undonePaints.add(paints.remove(paints.size() - 1));
            invalidate();
        }
    }

    public void clear() {
        paths.clear();
        paints.clear();
        undonePaths.clear();
        undonePaints.clear();
        invalidate();
    }

    public Bitmap getBitmap() {

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        draw(canvas);


        return bitmap;
    }
}