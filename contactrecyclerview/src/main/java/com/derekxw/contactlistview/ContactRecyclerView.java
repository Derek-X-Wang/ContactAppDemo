package com.derekxw.contactlistview;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.zakariya.stickyheaders.StickyHeaderLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/viethoa/recyclerview-alphabet-fast-scroller-android
 * Created by Derek on 5/17/2017.
 */

public class ContactRecyclerView extends LinearLayout implements
        AlphabetAdapter.OnItemClickListener,
        View.OnTouchListener {

    private RecyclerView recyclerView;
    private List<AlphabetItem> alphabets;
    private RecyclerView alphabetRecyclerView;
    private AlphabetAdapter alphabetAdapter;
    private boolean isInitialized = false;
    private int height;

    public interface BubbleTextGetter {
        String getTextToShowInBubble(int pos);
    }

    public interface ContactListPositionGetter {
        int getPositionWithFirstChar(char c);
    }

    public ContactRecyclerView(final Context context) {
        super(context);
        initialiseView(context);
    }

    public ContactRecyclerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initialiseView(context);
    }

    public ContactRecyclerView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialiseView(context);
    }

    protected void initialiseView(Context context) {
        if (isInitialized) {
            return;
        }

        // Init linear layout
        isInitialized = true;
        setOrientation(HORIZONTAL);
        setClipChildren(false);
        View.inflate(context, R.layout.contact_recycler_view, this);
        //final LayoutInflater inflater = LayoutInflater.from(getContext());
        //inflater.inflate(R.layout.fast_scroller, this, true);

        // Init alphabet recycler view
        alphabetRecyclerView = (RecyclerView) findViewById(R.id.alphabet);
        alphabetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alphabetRecyclerView.setOnTouchListener(this);
    }

    //----------------------------------------------------------------------------------------------
    //  Linear layout events
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        Log.d("tttttttt", "onTouchEvent: ");
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                setRecyclerViewPosition(y);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            final int itemCount = recyclerView.getAdapter().getItemCount();
            final float proportion = y / (float) height;
            final int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, 0);

            final String bubbleText = ((BubbleTextGetter) recyclerView.getAdapter()).getTextToShowInBubble(targetPos);
            setAlphabetWordSelected(bubbleText);
        }
    }

    //----------------------------------------------------------------------------------------------
    //  Implement events
    //----------------------------------------------------------------------------------------------

    @Override
    public void OnItemClicked(int alphabetPosition, int position) {
        // performSelectedAlphabetWord(position);
        takeRecyclerViewScrollToAlphabetPosition(position);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {

                Rect rect = new Rect();
                int childCount = alphabetRecyclerView.getChildCount();
                int[] listViewCoords = new int[2];
                alphabetRecyclerView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];

                View child;
                for (int i = 0; i < childCount; i++) {
                    child = alphabetRecyclerView.getChildAt(i);
                    child.getHitRect(rect);

                    // This is your pressed view
                    if (rect.contains(x, y)) {
                        LinearLayoutManager layoutManager = ((LinearLayoutManager)alphabetRecyclerView.getLayoutManager());
                        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                        int position = i + firstVisiblePosition;
                        // performSelectedAlphabetWord(position);
                        alphabetTouchEventOnItem(position);
                        break;
                    }
                }
                view.onTouchEvent(motionEvent);
            }
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------
    //  Alphabet Section
    //----------------------------------------------------------------------------------------------

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
//        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
//                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
//                    return;
//                }
//                final int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
//                final int verticalScrollRange = recyclerView.computeVerticalScrollRange();
//                final float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
//                setRecyclerViewPositionWithoutScrolling(height * proportion);
//            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//
//                }
//            }
//        };
//        this.recyclerView.addOnScrollListener(onScrollListener);
    }

    public void setUpAlphabet() {
        alphabets = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            char c = (char)('A' + i);
            alphabets.add(new AlphabetItem(i, String.valueOf(c), false));
        }
        // alphabets.add(new AlphabetItem(26, String.valueOf('#'), false));
        alphabetAdapter = new AlphabetAdapter(getContext(), alphabets);
        alphabetAdapter.setOnItemClickListener(this);
        alphabetRecyclerView.setAdapter(alphabetAdapter);
    }

    public void setUpAlphabet(List<AlphabetItem> alphabetItems) {
        if (alphabetItems == null || alphabetItems.size() <= 0)
            return;

        alphabets = alphabetItems;
        alphabetAdapter = new AlphabetAdapter(getContext(), alphabets);
        alphabetAdapter.setOnItemClickListener(this);
        alphabetRecyclerView.setAdapter(alphabetAdapter);
    }

    private void setRecyclerViewPositionWithoutScrolling(float y) {
        if (recyclerView != null) {
            final int itemCount = recyclerView.getAdapter().getItemCount();
            final float proportion = y / (float) height;
            final int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            final String bubbleText = ((BubbleTextGetter) recyclerView.getAdapter()).getTextToShowInBubble(targetPos);
            setAlphabetWordSelected(bubbleText);
        }
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    private void performSelectedAlphabetWord(int position) {
        if (position < 0 || position >= alphabets.size()) {
            return;
        }

        for (AlphabetItem alphabetItem : alphabets) {
            alphabetItem.isActive = false;
        }

        alphabets.get(position).isActive = true;
        alphabetAdapter.refreshDataChange(alphabets);
    }

    private void alphabetTouchEventOnItem(int position) {
        if (alphabets == null || position < 0 || position >= alphabets.size()) {
            return;
        }

        takeRecyclerViewScrollToAlphabetPosition(alphabets.get(position).position);
    }

    private void takeRecyclerViewScrollToAlphabetPosition(int position) {
        if (recyclerView == null || recyclerView.getAdapter() == null) {
            return;
        }

        int count = recyclerView.getAdapter().getItemCount();
        if (position < 0 || position > count) {
            return;
        }

        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        ContactListPositionGetter getter = (ContactListPositionGetter) adapter;
        char first = alphabets.get(position).word.charAt(0);
        int recyclerPosition = getter.getPositionWithFirstChar(first);
        Log.d("tttttttt", "takeRecyclerViewScrollToAlphabetPosition: "+recyclerPosition);
        ((StickyHeaderLayoutManager) recyclerView.getLayoutManager()).scrollToPosition(recyclerPosition);
    }

    private void setAlphabetWordSelected(String bubbleText) {
        if (bubbleText == null || bubbleText.trim().isEmpty()) {
            return;
        }

        for (int i = 0; i < alphabets.size(); i++) {
            AlphabetItem alphabetItem = alphabets.get(i);
            if (alphabetItem == null || alphabetItem.word.trim().isEmpty()) {
                continue;
            }

            if (alphabetItem.word.equals(bubbleText)) {
                // performSelectedAlphabetWord(i);
                alphabetRecyclerView.smoothScrollToPosition(i);
                break;
            }
        }
    }
}
