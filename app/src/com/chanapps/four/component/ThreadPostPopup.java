package com.chanapps.four.component;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.chanapps.four.activity.R;
import com.chanapps.four.activity.RefreshableActivity;
import com.chanapps.four.activity.SettingsActivity;
import com.chanapps.four.adapter.AbstractThreadCursorAdapter;
import com.chanapps.four.data.ChanFileStorage;
import com.chanapps.four.data.ChanPost;
import com.chanapps.four.data.ChanThread;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 2/2/13
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class ThreadPostPopup extends BoardThreadPopup {

    public static final String TAG = ThreadPostPopup.class.getSimpleName();
    protected static final boolean DEBUG = false;

    protected AbstractThreadCursorAdapter adapter;

    public ThreadPostPopup(RefreshableActivity activity,
                           LayoutInflater layoutInflater,
                           ImageLoader imageLoader,
                           DisplayImageOptions displayImageOptions,
                           AbstractThreadCursorAdapter adapter)
    {
        super(activity, layoutInflater, imageLoader, displayImageOptions);
        this.adapter = adapter;
    }

    @Override
    protected void setGoToThreadButton(final AdapterView<?> adapterView, final View view, final int position, final long id) {
        goToThreadButtonLine.setVisibility(View.GONE); // we are already at thread level
        goToThreadButton.setVisibility(View.GONE); // we are already at thread level
    }

    @Override
    protected void displayHighlightRepliesButton(final String boardCode, final long threadNo, final long postNo) { // board-level doesn't highlight, only thread-level does
        if (postNo > 0) {
            highlightRepliesButtonLine.setVisibility(View.VISIBLE);
            highlightRepliesButton.setVisibility(View.VISIBLE);
            highlightRepliesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HighlightRepliesTask task = new HighlightRepliesTask(activity.getBaseContext(), adapter, boardCode, threadNo);
                    task.execute(postNo);
                    popupWindow.dismiss();
                }
            });
        }
        else {
            highlightRepliesButtonLine.setVisibility(View.GONE);
            highlightRepliesButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void displayHighlightIdButton(final String boardCode, final long threadNo, final long postNo,
                                            final String userId, final String tripcode, final String name, final String email)
    { // board-level doesn't highlight, only thread-level does
        if (userId != null && !userId.isEmpty()) {
            highlightIdButtonLine.setVisibility(View.VISIBLE);
            highlightIdButton.setVisibility(View.VISIBLE);
            highlightIdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HighlightIdTask task = new HighlightIdTask(activity.getBaseContext(), adapter, boardCode, threadNo, userId);
                    task.execute(postNo);
                    popupWindow.dismiss();
                }
            });
        }
        else if (tripcode != null && !tripcode.isEmpty()) {
            highlightIdButtonLine.setVisibility(View.VISIBLE);
            highlightIdButton.setVisibility(View.VISIBLE);
            highlightIdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HighlightTripcodeTask task = new HighlightTripcodeTask(activity.getBaseContext(), adapter, boardCode, threadNo, tripcode);
                    task.execute(postNo);
                    popupWindow.dismiss();
                }
            });
        }
        else if (name != null && !name.isEmpty() && !name.equalsIgnoreCase("Anonymous")) {
            highlightIdButtonLine.setVisibility(View.VISIBLE);
            highlightIdButton.setVisibility(View.VISIBLE);
            highlightIdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HighlightNameTask task = new HighlightNameTask(activity.getBaseContext(), adapter, boardCode, threadNo, name);
                    task.execute(postNo);
                    popupWindow.dismiss();
                }
            });
        }
        else if (email != null && !email.isEmpty() && !email.equalsIgnoreCase("sage")) {
            highlightIdButtonLine.setVisibility(View.VISIBLE);
            highlightIdButton.setVisibility(View.VISIBLE);
            highlightIdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HighlightEmailTask task = new HighlightEmailTask(activity.getBaseContext(), adapter, boardCode, threadNo, email);
                    task.execute(postNo);
                    popupWindow.dismiss();
                }
            });
        }
        else {
            highlightIdButtonLine.setVisibility(View.GONE);
            highlightIdButton.setVisibility(View.GONE);
        }
    }

    private class HighlightRepliesTask extends AsyncTask<Long, Void, String> {
        private Context context = null;
        private AbstractThreadCursorAdapter threadAdapter = null;
        private String boardCode = null;
        private long threadNo = 0;
        public HighlightRepliesTask(Context context, AbstractThreadCursorAdapter adapter, String boardCode, long threadNo) {
            this.context = context;
            this.threadAdapter = adapter;
            this.boardCode = boardCode;
            this.threadNo = threadNo;
        }
        @Override
        protected String doInBackground(Long... postNos) {
            String result = null;
            long postNo = postNos[0];
            long[] prevPosts = null;
            long[] nextPosts = null;
            try {
                ChanThread thread = ChanFileStorage.loadThreadData(context, boardCode, threadNo);
                if (thread != null) {
                    prevPosts = thread.getPrevPostsReferenced(postNo);
                    nextPosts = thread.getNextPostsReferredTo(postNo);
                }
                else {
                    result = context.getString(R.string.thread_couldnt_load);
                    Log.e(TAG, "Coludn't load thread " + boardCode + "/" + threadNo);
                }
            }
            catch (Exception e) {
                result = context.getString(R.string.thread_couldnt_load);
                Log.e(TAG, "Exception while getting thread post highlights", e);
            }
            threadAdapter.setHighlightPostReplies(postNo, prevPosts, nextPosts);
            if ((prevPosts == null || prevPosts.length == 0) && (nextPosts == null || nextPosts.length == 0)) {
                result = context.getString(R.string.thread_no_replies_found);
            }
            else {
                String msg = context.getString(R.string.thread_replies_found);
                int count = (prevPosts == null ? 0 : prevPosts.length) + (nextPosts == null ? 0 : nextPosts.length);
                result = String.format(msg, count);
            }
            if (DEBUG) Log.i(TAG, "Set highlight posts prev=" + Arrays.toString(prevPosts) + " next=" + Arrays.toString(nextPosts));
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            threadAdapter.notifyDataSetChanged();
        }
    }

    private class HighlightIdTask extends AsyncTask<Long, Void, String> {
        private Context context = null;
        private AbstractThreadCursorAdapter threadAdapter = null;
        private String boardCode = null;
        private long threadNo = 0;
        private String userId = null;
        public HighlightIdTask(Context context, AbstractThreadCursorAdapter adapter, String boardCode, long threadNo, String userId) {
            this.context = context;
            this.threadAdapter = adapter;
            this.boardCode = boardCode;
            this.threadNo = threadNo;
            this.userId = userId;
        }
        @Override
        protected String doInBackground(Long... postNos) {
            String result = null;
            long postNo = postNos[0];
            long[] idPosts = null;
            try {
                ChanThread thread = ChanFileStorage.loadThreadData(context, boardCode, threadNo);
                if (thread != null) {
                    idPosts = thread.getIdPosts(postNo, userId);
                }
                else {
                    result = context.getString(R.string.thread_couldnt_load);
                    Log.e(TAG, "Coludn't load thread " + boardCode + "/" + threadNo);
                }
            }
            catch (Exception e) {
                result = context.getString(R.string.thread_couldnt_load);
                Log.e(TAG, "Exception while getting thread post highlights", e);
            }
            threadAdapter.setHighlightPostsWithId(postNo, idPosts);
            boolean useFriendlyIds = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext()).getBoolean(SettingsActivity.PREF_USE_FRIENDLY_IDS, true);
            String formattedUserId = ChanPost.getUserId(userId, useFriendlyIds);
            if ((idPosts == null || idPosts.length == 0)) {
                result = String.format(context.getString(R.string.thread_no_id_found), formattedUserId);
            }
            else {
                result = String.format(context.getString(R.string.thread_id_found), idPosts.length, formattedUserId);
            }
            if (DEBUG) Log.i(TAG, "Set highlight posts for id=" + userId + " posts=" + Arrays.toString(idPosts));
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            threadAdapter.notifyDataSetChanged();
        }
    }

    private class HighlightTripcodeTask extends AsyncTask<Long, Void, String> {
        private Context context = null;
        private AbstractThreadCursorAdapter threadAdapter = null;
        private String boardCode = null;
        private long threadNo = 0;
        private String tripcode = null;
        public HighlightTripcodeTask(Context context, AbstractThreadCursorAdapter adapter, String boardCode, long threadNo, String tripcode) {
            this.context = context;
            this.threadAdapter = adapter;
            this.boardCode = boardCode;
            this.threadNo = threadNo;
            this.tripcode = tripcode;
        }
        @Override
        protected String doInBackground(Long... postNos) {
            String result = null;
            long postNo = postNos[0];
            long[] tripcodePosts = null;
            try {
                ChanThread thread = ChanFileStorage.loadThreadData(context, boardCode, threadNo);
                if (thread != null) {
                    tripcodePosts = thread.getTripcodePosts(postNo, tripcode);
                }
                else {
                    result = context.getString(R.string.thread_couldnt_load);
                    Log.e(TAG, "Coludn't load thread " + boardCode + "/" + threadNo);
                }
            }
            catch (Exception e) {
                result = context.getString(R.string.thread_couldnt_load);
                Log.e(TAG, "Exception while getting thread post highlights", e);
            }
            threadAdapter.setHighlightPostsWithId(postNo, tripcodePosts);
            if ((tripcodePosts == null || tripcodePosts.length == 0)) {
                result = String.format(context.getString(R.string.thread_no_id_found), tripcode);
            }
            else {
                result = String.format(context.getString(R.string.thread_id_found), tripcodePosts.length, tripcode);
            }
            if (DEBUG) Log.i(TAG, "Set highlight posts for tripcode=" + tripcode + " posts=" + Arrays.toString(tripcodePosts));
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            threadAdapter.notifyDataSetChanged();
        }
    }

    private class HighlightNameTask extends AsyncTask<Long, Void, String> {
        private Context context = null;
        private AbstractThreadCursorAdapter threadAdapter = null;
        private String boardCode = null;
        private long threadNo = 0;
        private String name = null;
        public HighlightNameTask(Context context, AbstractThreadCursorAdapter adapter, String boardCode, long threadNo, String name) {
            this.context = context;
            this.threadAdapter = adapter;
            this.boardCode = boardCode;
            this.threadNo = threadNo;
            this.name = name;
        }
        @Override
        protected String doInBackground(Long... postNos) {
            String result = null;
            long postNo = postNos[0];
            long[] namePosts = null;
            try {
                ChanThread thread = ChanFileStorage.loadThreadData(context, boardCode, threadNo);
                if (thread != null) {
                    namePosts = thread.getNamePosts(postNo, name);
                }
                else {
                    result = context.getString(R.string.thread_couldnt_load);
                    Log.e(TAG, "Coludn't load thread " + boardCode + "/" + threadNo);
                }
            }
            catch (Exception e) {
                result = context.getString(R.string.thread_couldnt_load);
                Log.e(TAG, "Exception while getting thread post highlights", e);
            }
            threadAdapter.setHighlightPostsWithId(postNo, namePosts);
            if ((namePosts == null || namePosts.length == 0)) {
                result = String.format(context.getString(R.string.thread_no_id_found), name);
            }
            else {
                result = String.format(context.getString(R.string.thread_id_found), namePosts.length, name);
            }
            if (DEBUG) Log.i(TAG, "Set highlight posts for name=" + name + " posts=" + Arrays.toString(namePosts));
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            threadAdapter.notifyDataSetChanged();
        }
    }

    private class HighlightEmailTask extends AsyncTask<Long, Void, String> {
        private Context context = null;
        private AbstractThreadCursorAdapter threadAdapter = null;
        private String boardCode = null;
        private long threadNo = 0;
        private String email = null;
        public HighlightEmailTask(Context context, AbstractThreadCursorAdapter adapter, String boardCode, long threadNo, String email) {
            this.context = context;
            this.threadAdapter = adapter;
            this.boardCode = boardCode;
            this.threadNo = threadNo;
            this.email = email;
        }
        @Override
        protected String doInBackground(Long... postNos) {
            String result = null;
            long postNo = postNos[0];
            long[] emailPosts = null;
            try {
                ChanThread thread = ChanFileStorage.loadThreadData(context, boardCode, threadNo);
                if (thread != null) {
                    emailPosts = thread.getEmailPosts(postNo, email);
                }
                else {
                    result = context.getString(R.string.thread_couldnt_load);
                    Log.e(TAG, "Coludn't load thread " + boardCode + "/" + threadNo);
                }
            }
            catch (Exception e) {
                result = context.getString(R.string.thread_couldnt_load);
                Log.e(TAG, "Exception while getting thread post highlights", e);
            }
            threadAdapter.setHighlightPostsWithId(postNo, emailPosts);
            if ((emailPosts == null || emailPosts.length == 0)) {
                result = String.format(context.getString(R.string.thread_no_id_found), email);
            }
            else {
                result = String.format(context.getString(R.string.thread_id_found), emailPosts.length, email);
            }
            if (DEBUG) Log.i(TAG, "Set highlight posts for email=" + email + " posts=" + Arrays.toString(emailPosts));
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            threadAdapter.notifyDataSetChanged();
        }
    }

}
