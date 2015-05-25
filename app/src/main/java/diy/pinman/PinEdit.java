package diy.pinman;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Field;

/**
 * PIN edit -- an editor designed to edit PIN.
 */
public class PinEdit extends EditText {

    private static final String TAG = "PinEdit";

    // elapse time, before hide PIN
    private static final int ELAPSE = 1000;

    // defaults constants
    private static final int DEFAULT_CHAR_WIDTH = 48;
    private static final int DEFAULT_CHAR_HEIGHT = 48;
    private static final int DEFAULT_SPACE_BETWEEN_CHARS = 8;

    // number of PIN characters, gets from GUI editor android:maxLength, default is here
    private int maxLength = 4;

    // single character's width and height
    private int charWidth = 0;
    private int charHeight = 0;
    // a drawable background of each characters
    private Drawable charDrawable = null;
    // a drawable background of each characters that typed
    private Drawable typedCharDrawable = null;
    // a drawable that whole background
    private int background = 0x00000000;
    // number of pixels that keep characters space
    private int spaceBetweenChars = 0;
    // hides characters, just use drawables
    private boolean hideChars = false;
    // this is workaround to fix gravity center_horizontal and end
    // use getGravityFix() and setGravityFix() instead!
    private int gravity = Gravity.START | Gravity.TOP;

    // if you want to do action when user type complete
    private OnCompleteListener onCompleteListener = null;

    // adds characters and hide after time
    private boolean addChar = false;
    private Runnable runnable = null;
    private Handler handler = null;

    // canvas related
    private Paint paint;
    private int textHeight;
    private Rect bounds;
    private Rect boundsAll;
    private Rect container;
    private Rect rect;

    public PinEdit(Context context) {
        super(context);
        initialize(context, null, 0);
        preCompute();
    }

    public PinEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
        preCompute();
    }

    public PinEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
        preCompute();
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        // keeps maxLength variable for using later
        maxLength = getMaxLength();

        // gets display metrics for later used
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        // we have to set background to 0x00000000, otherwise, it will show blue line
        super.setBackgroundColor(0x00000000);

        // sets various parameters
        setCursorVisible(false);
        setCustomSelectionActionModeCallback(null);
        setFocusable(true);
        setFocusableInTouchMode(true);

        // disables menu cut, copy, paste
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        // adds TextWatcher for transform password and calls OnCompleteListener
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count < after) {
                    handler.removeCallbacks(runnable);
                    addChar = true;
                    handler.postDelayed(runnable, ELAPSE);
                }
                else
                    addChar = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= maxLength) {
                    if (onCompleteListener != null)
                        onCompleteListener.onComplete(s.toString());
                }
            }
        });

        // creates runnable and handler, for timer
        runnable = new Runnable() {
            @Override
            public void run() {
                addChar = false;
                invalidate();
            }
        };
        handler = new Handler();

        if (attrs != null) {
            TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PinEdit, 0, 0);
            try {
                charWidth = array.getDimensionPixelOffset(R.styleable.PinEdit_charWidth, (int) (DEFAULT_CHAR_WIDTH * metrics.density));
                charHeight = array.getDimensionPixelOffset(R.styleable.PinEdit_charHeight, (int) (DEFAULT_CHAR_HEIGHT * metrics.density));
                charDrawable = array.getDrawable(R.styleable.PinEdit_charDrawable);
                typedCharDrawable = array.getDrawable(R.styleable.PinEdit_typedCharDrawable);
                background = array.getColor(R.styleable.PinEdit_background, 0x00000000);
                spaceBetweenChars = array.getDimensionPixelOffset(R.styleable.PinEdit_spaceBetweenChars, (int) (DEFAULT_SPACE_BETWEEN_CHARS * metrics.density));
                hideChars = array.getBoolean(R.styleable.PinEdit_hideChars, false);

                // workaround fixes about gravity!
                int gravity = super.getGravity();
                super.setGravity(Gravity.LEFT | Gravity.TOP);
                this.gravity = gravity;
            }
            finally {
                array.recycle();
            }
        }
        else {
            charWidth = (int) (DEFAULT_CHAR_WIDTH * metrics.density);
            charHeight = (int) (DEFAULT_CHAR_HEIGHT * metrics.density);
            spaceBetweenChars = (int) (DEFAULT_SPACE_BETWEEN_CHARS * metrics.density);
        }
    }

    private void preCompute() {
        // gets paint for drawing
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        // gets text align
        Rect textBounds = new Rect();
        paint.setTextSize(getTextSize());
        paint.getTextBounds("8", 0, 1, textBounds);
        textHeight = textBounds.height();

        // gets single maximum whether text or drawable
        bounds = new Rect();
        bounds.left = 0;
        bounds.top = 0;
        bounds.right = charWidth;
        bounds.bottom = charHeight;

        // gets all characters size, both text and drawable
        boundsAll = new Rect();
        boundsAll.set(0, 0, bounds.width() * maxLength + spaceBetweenChars * (maxLength - 1), bounds.height());

        // pre-creates container for onDraw()
        container = new Rect();
        rect = new Rect();
    }

    @Override
    public void onDraw(Canvas canvas) {
        //super.onDraw(canvas);

        canvas.drawColor(background);

        container.set(getPaddingLeft(), getPaddingTop(), canvas.getWidth() - getPaddingRight(), canvas.getHeight() - getPaddingBottom());
        rect.set(boundsAll);

        // offsets bounds all to match gravity
        Log.d(TAG, String.format("before container %d, %d - %d, %d rect %d, %d - %d, %d",
                container.left, container.top, container.right, container.bottom,
                rect.left, rect.top, rect.right, rect.bottom));
        Gravity.apply(this.gravity, boundsAll.width(), boundsAll.height(), container, rect);
        /*
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(0xFFFF0000);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        paint.setColor(0xFF00FF00);
        canvas.drawRect(container.left, container.top, container.right, container.bottom, paint);
        paint.setColor(0xFF0000FF);
        canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, paint);
        */
        Log.d(TAG, String.format("after container %d, %d - %d, %d rect %d, %d - %d, %d",
                container.left, container.top, container.right, container.bottom,
                rect.left, rect.top, rect.right, rect.bottom));

        // draws drawables
        paint.setColor(0xFF000000);
        paint.setStyle(Paint.Style.FILL);
        if (typedCharDrawable != null) {
            int start = rect.left;
            int length = length();
            for (int i = 0; i < length; i++) {
                int left = start + i * bounds.width();
                typedCharDrawable.setBounds(left, rect.top, left + bounds.width(), rect.bottom);
                typedCharDrawable.draw(canvas);
                start += spaceBetweenChars;
            }
        }
        if (charDrawable != null) {
            int begin = typedCharDrawable != null ? length() : 0;
            int start = rect.left + begin * spaceBetweenChars;
            for (int i = begin; i < maxLength; i++) {
                int left = start + i * bounds.width();
                charDrawable.setBounds(left, rect.top, left + bounds.width(), rect.bottom);
                charDrawable.draw(canvas);
                start += spaceBetweenChars;
            }
        }

        // draws text
        if (!hideChars) {
            paint.setColor(0xFF000000);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            int x = rect.left + bounds.width() / 2;
            int y = rect.top + bounds.height() / 2 + textHeight / 2;
            int start = 0;
            String text = getText().toString();
            for (int i = 0; i < text.length(); i++) {
                canvas.drawText(transform(text.substring(i, i + 1), i), start + x + i * bounds.width(), y, paint);
                start += spaceBetweenChars;
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST)
            width = getPaddingLeft() + boundsAll.width() + getPaddingRight();
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST)
            height = getPaddingTop() + boundsAll.height() + getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        preCompute();
        invalidate();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        }
        else {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        setSelection(length());
    }

    private String transform(String in, int position) {
        if (addChar && position + 1 >= length())
            return in;
        else
            return "\u2022";
    }

    private int getMaxLength() {
        // gets max length from GUI editor, or use default value
        int maxLength = this.maxLength;
        for (InputFilter filter : getFilters()) {
            if (filter instanceof InputFilter.LengthFilter) {
                try {
                    Field maxLengthField = filter.getClass().getDeclaredField("mMax");
                    maxLengthField.setAccessible(true);
                    if (maxLengthField.isAccessible())
                        maxLength = maxLengthField.getInt(filter);
                }
                catch (IllegalAccessException e) {
                    Log.w(filter.getClass().getName(), e);
                }
                catch (IllegalArgumentException e) {
                    Log.w(filter.getClass().getName(), e);
                }
                catch (NoSuchFieldException e) {
                    Log.w(filter.getClass().getName(), e);
                } // if an Exception is thrown, Log it and return -1
            }
        }

        // minimum must be at least 1
        if (maxLength < 1)
            maxLength = 1;

        // sets maxLength back into EditText
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(maxLength);
        setFilters(filters);

        return maxLength;
    }

    /**
     * Clears all text.
     */
    public void clear() {
        setText("");
        invalidate();
    }

    /**
     * Gets character width.
     */
    public int getCharWidth() {
        return charWidth;
    }

    /**
     * Gets character height.
     */
    public int getCharHeight() {
        return charHeight;
    }

    /**
     * Sets character width and height.
     */
    public void setCharSize(int width, int height) {
        charWidth = width;
        charHeight = height;
        preCompute();
        invalidate();
    }

    /**
     * Gets character drawable.
     */
    public Drawable getCharDrawable() {
        return charDrawable;
    }

    /**
     * Sets character drawable.
     */
    public void setCharDrawable(Drawable value) {
        charDrawable = value;
        preCompute();
        invalidate();
    }

    /**
     * Gets typed character drawable.
     */
    public Drawable getTypedCharDrawable() {
        return typedCharDrawable;
    }

    /**
     * Sets typed character drawable.
     */
    public void setTypedCharDrawable(Drawable value) {
        typedCharDrawable = value;
        preCompute();
        invalidate();
    }

    /**
     * Gets background color.
     */
    public int getBackgroundColor() {
        return background;
    }

    /**
     * Sets background color.
     */
    public void setBackgroundColor(int value) {
        background = value;
        preCompute();
        invalidate();
    }

    /**
     * Gets space between characters.
     */
    public int getSpaceBetweenChars() {
        return spaceBetweenChars;
    }

    /**
     * Sets space between characters.
     */
    public void setSpaceBetweenChars(int value) {
        spaceBetweenChars = value;
        preCompute();
        invalidate();
    }

    /**
     * Gets hiding characters.
     */
    public boolean getHideChars() {
        return hideChars;
    }

    /**
     * Sets hiding characters.
     */
    public void setHideChars(boolean value) {
        hideChars = value;
        invalidate();
    }

    /**
     * Gets gravity.
     */
    public int getGravityFix() {
        return gravity;
    }

    /**
     * Sets gravity.
     */
    public void setGravityFix(int value) {
        gravity = value;
        preCompute();
        invalidate();
    }

    /**
     * Gets the OnCompleteListener.
     */
    public OnCompleteListener getOnCompleteListener() {
        return onCompleteListener;
    }

    /**
     * Sets the OnCompleteListener.
     */
    public void setOnCompleteListener(OnCompleteListener value) {
        onCompleteListener = value;
    }

    /**
     * Calls when length() == maxLength
     */
    public interface OnCompleteListener {
        void onComplete(String text);
    }

}
