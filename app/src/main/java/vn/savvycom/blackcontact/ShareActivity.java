package vn.savvycom.blackcontact;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.io.IOException;

import io.fabric.sdk.android.Fabric;


public class ShareActivity extends BaseActivity implements View.OnClickListener {

    private static final long LIMIT = 125829120; // 120MB
    private static final String[] SHARE_OVER = {"Facebook", "Twitter", "Email"};
    FrameLayout shareContent;
    int shareType = 0;
    Button link, photo, video;
    String shareFilePath, shareLink;
    ShareContent content;
    public static final int LINK_TYPE = 1;
    public static final int PHOTO_TYPE = 2;
    public static final int VIDEO_TYPE = 3;
    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        FacebookSdk.sdkInitialize(getApplicationContext());
        Fabric.with(this, new TweetComposer());
        shareDialog = new ShareDialog(this);
        setTitle("Share all you have!");
        shareContent = (FrameLayout) findViewById(R.id.share_content);
        link = (Button) findViewById(R.id.btn_link);
        photo = (Button) findViewById(R.id.btn_photo);
        video = (Button) findViewById(R.id.btn_video);
    }

    private void highlightButton(int type) {
        switch (type) {
            case LINK_TYPE:
                link.setTextColor(getResources().getColor(R.color.md_red_500));
                photo.setTextColor(getResources().getColor(R.color.md_green_500));
                video.setTextColor(getResources().getColor(R.color.md_green_500));
                break;
            case PHOTO_TYPE:
                link.setTextColor(getResources().getColor(R.color.md_green_500));
                photo.setTextColor(getResources().getColor(R.color.md_red_500));
                video.setTextColor(getResources().getColor(R.color.md_green_500));
                break;
            case VIDEO_TYPE:
                link.setTextColor(getResources().getColor(R.color.md_green_500));
                photo.setTextColor(getResources().getColor(R.color.md_green_500));
                video.setTextColor(getResources().getColor(R.color.md_red_500));
                break;
            default:
                link.setTextColor(getResources().getColor(R.color.md_green_500));
                photo.setTextColor(getResources().getColor(R.color.md_green_500));
                video.setTextColor(getResources().getColor(R.color.md_green_500));
                break;
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_share;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_link:
                shareType = LINK_TYPE;
                highlightButton(shareType);
                shareContent.removeAllViews();
                View v = LayoutInflater.from(this).inflate(R.layout.share_link_child_layout, shareContent);
                ((EditText) v.findViewById(R.id.content_link)).requestFocus();
                break;
            case R.id.btn_photo:
                shareType = PHOTO_TYPE;
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, shareType);
                break;
            case R.id.btn_video:
                shareType = VIDEO_TYPE;
                Intent videoPickerIntent = new Intent(Intent.ACTION_PICK);
                videoPickerIntent.setType("video/*");
                startActivityForResult(videoPickerIntent, shareType);
                break;
            case R.id.thumbs:
                if (shareType == PHOTO_TYPE) photo.callOnClick();
                else video.callOnClick();
                break;
            case R.id.btn_share:
                if (shareType == LINK_TYPE) {
                    shareLink = ((EditText) shareContent.getChildAt(0).findViewById(R.id.content_link)).getText().toString();
                    if (shareLink.equals("")) {
                        Toast.makeText(this, "Link is empty, wth do you want to share? Nothing?", Toast.LENGTH_LONG).show();
                        return;
                    }
                    content = new ShareLinkContent.Builder().setContentUrl(Uri.parse(shareLink)).build();
                }
                final String des = ((EditText) findViewById(R.id.desc)).getText().toString();
                new MaterialDialog.Builder(this)
                        .title("Share over...")
                        .items(SHARE_OVER)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        shareDialog.show(content);
                                        break;
                                    case 1:
                                        TweetComposer.Builder builder = new TweetComposer.Builder(ShareActivity.this)
                                                .text(des)
                                                .image(Uri.fromFile(new File(shareFilePath)));
                                        builder.show();
                                        break;
                                    case 2:
                                        if (shareType != LINK_TYPE) {
                                            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                                            emailIntent.setType("application/image");
                                            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, des);
                                            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(shareFilePath)));
                                            startActivity(Intent.createChooser(emailIntent, "Choose an email client..."));
                                        } else {
                                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                                            intent.setData(Uri.parse("mailto:"));
                                            String fullContent = des + "<br>" + "<a href=\"" + shareLink + "\">" + shareLink + "</a>";
                                            intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(fullContent));
                                            if (intent.resolveActivity(getPackageManager()) != null) {
                                                startActivity(intent);
                                            }
                                        }
                                }
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_TYPE) {
            if (resultCode == RESULT_OK) {
                Uri selectedPhotoUri = data.getData();
                new CacheImageTask(this).execute(selectedPhotoUri);
            }
        } else if (requestCode == VIDEO_TYPE) {
            if (resultCode == RESULT_OK) {
                Uri selectedVideoUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedVideoUri, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                shareFilePath = cursor.getString(columnIndex);
                cursor.close();

                File file = new File(shareFilePath);

                if (file.length() > LIMIT) {
                    Toast.makeText(this, "Cannot share video over 12MB", Toast.LENGTH_LONG).show();
                } else {
                    highlightButton(shareType);
                    shareContent.removeAllViews();
                    View videoView = LayoutInflater.from(this).inflate(R.layout.share_video_child_layout, shareContent);
//                    Bitmap bMap = ThumbnailUtils.createVideoThumbnail(shareFilePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
//                    ImageView imageView = (ImageView) videoView.findViewById(R.id.thumbs);
//                    imageView.setImageBitmap(bMap);
//                    imageView.setOnClickListener(this);
                    VideoView videoPanel = (VideoView) videoView.findViewById(R.id.video_panel);
                    MediaController controller = new MediaController(this);
                    videoPanel.setMediaController(controller);
                    videoPanel.setVideoPath(file.getAbsolutePath());
                    videoPanel.start();
                    ((TextView) videoView.findViewById(R.id.file_name)).setText(shareFilePath);
                    ShareVideo video = new ShareVideo.Builder().setLocalUrl(selectedVideoUri).build();
                    content = new ShareVideoContent.Builder().setVideo(video).build();
                }
            }
        }
    }

    public void onCacheImageComplete(Bitmap bitmap, String filePath) {
        shareFilePath = filePath;
        highlightButton(shareType);
        shareContent.removeAllViews();
        View photoView = LayoutInflater.from(this).inflate(R.layout.share_photo_child_layout, shareContent);
        ((TextView) photoView.findViewById(R.id.file_name)).setText(filePath);
        try {
            ImageView imageView = (ImageView) photoView.findViewById(R.id.thumbs);
            imageView.setOnClickListener(this);

            //imageView.setImageBitmap(bitmap);
            Picasso.with(this)
                    .load(new File(filePath))
                    .resize(500, 500)
                    .onlyScaleDown()
                    .centerInside()
                    .into(imageView);
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            content = new SharePhotoContent.Builder().addPhoto(photo).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CacheImageTask extends AsyncTask<Uri, Void, Bitmap> {

        Context context;
        MaterialDialog dialog;
        String filePath;
        Boolean done = false;

        public CacheImageTask(Context context) {
            this.context = context;
            dialog = new MaterialDialog.Builder(context)
                    .title("Loading")
                    .content("Wait a bit...")
                    .progress(true, 0)
                    .autoDismiss(false)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            if (!done) CacheImageTask.this.cancel(true);
                        }
                    })
                    .show();
        }

        @Override
        protected Bitmap doInBackground(Uri... uris) {
            Uri selectedPhotoUri = uris[0];
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedPhotoUri, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();

            File file = new File(filePath);
            if (file.length() > LIMIT) {
                Toast.makeText(context, "Cannot share photo over 12MB", Toast.LENGTH_LONG).show();
                this.cancel(true);
                return null;
            }
            try {
                return Picasso.with(context)
                        .load(selectedPhotoUri)
                        .get();
            } catch (IOException e) {
                this.cancel(true);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            done = true;
            dialog.dismiss();
            ((ShareActivity) context).onCacheImageComplete(bitmap, filePath);
        }

        @Override
        protected void onCancelled(Bitmap result) {
            super.onCancelled(result);
            dialog.dismiss();
        }
    }
}
