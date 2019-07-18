package app.photils.keywhat;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import app.photils.Keywhat;

public class KeywhatViewModel extends AndroidViewModel {
    public final static String SUGGESTION_KEY = "Suggestions";
    private Uri mActiveUri;
    private boolean mAliasEnabled = true;
    private CustomTagModel mModel;
    private MutableLiveData<HashMap<Integer, ArrayList<KeywhatTag>>> mTags = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> mGroups = new MutableLiveData<>();
    private int mSelectedCounter = 0;
    private HashSet<String> mAvailableCustomTags = new HashSet<>();
    private List<String> mCachedSuggestions;



    public KeywhatViewModel(Application application) {
        super(application);
        mModel = new CustomTagModel(application);
        loadTags();
    }

    public MutableLiveData<ArrayList<String>> getGroups() {
        return mGroups;
    }

    public MutableLiveData<HashMap<Integer, ArrayList<KeywhatTag>>> getTags() {
        return mTags;
    }

    public Uri getActiveUri() {
        return mActiveUri;
    }

    public void setActiveUri(Uri mActiveUri) {
        this.mActiveUri = mActiveUri;
    }

    public boolean isAliasEnabled() {
        return mAliasEnabled;
    }

    public void setAliasEnabled(boolean mAliasEnabled) {
        this.mAliasEnabled = mAliasEnabled;
    }


    public void loadTags() {
        mSelectedCounter = 0;
        mAvailableCustomTags.clear();
        List<CustomTagGroup> groups = mModel.getTagGroups();

        ArrayList<String> groupList = new ArrayList<>();
        HashMap<Integer, ArrayList<KeywhatTag>> tagsList = new HashMap<>();

        groupList.add(SUGGESTION_KEY);
        tagsList.put(0, new ArrayList<>());

        int idx = 1; // 1 because suggestions is 0
        for(CustomTagGroup group : groups) {
            groupList.add(group.group);

            int groupid = idx * CustomTagModel.TAG_PER_GROUP_LIMIT;
            List<CustomTag> tags = mModel.getTagByGroup(group.group);
            ArrayList<KeywhatTag> groupedTags = new ArrayList<>();
            int tid = 0;
            for(CustomTag tag : tags) {
                groupedTags.add(new KeywhatTag(groupid + (tid++), tag.name, tag.isDefault));
                if(tag.isDefault) mSelectedCounter++;

                mAvailableCustomTags.add(tag.name.toLowerCase());
            }

            tagsList.put(groupid, groupedTags);
            idx++;
        }

        getTags().setValue(tagsList);
        getGroups().setValue(groupList);


        if(mCachedSuggestions != null)
            addSuggestionKeyword(mCachedSuggestions);
    }

    public void addSuggestionKeyword(List<String> tagList) {
        mCachedSuggestions = tagList;

        HashMap<Integer, ArrayList<KeywhatTag>> tags = getTags().getValue();
        ArrayList<KeywhatTag> tagGroup = tags.get(0);
        tagGroup.clear();

        int idx = 0;
        for(String tag : tagList) {
            // skip suggestion if already exist in custom tags
            if(mAvailableCustomTags.contains(tag.toLowerCase()))
                continue;

            tagGroup.add(new KeywhatTag(idx++, tag, false));
        }

        getTags().setValue(tags);
    }

    public void setTagSelected(int tid) {
        int gid = tid / CustomTagModel.TAG_PER_GROUP_LIMIT * CustomTagModel.TAG_PER_GROUP_LIMIT;
        HashMap<Integer, ArrayList<KeywhatTag>> tags = getTags().getValue();
        for(KeywhatTag tag : tags.get(gid)) {
            if(tag.getTid() == tid)
                tag.setSelected(true);
        }

        mSelectedCounter++;

        getTags().setValue(tags);
    }

    public void setTagUnselected(int tid) {
        int gid = tid / CustomTagModel.TAG_PER_GROUP_LIMIT * CustomTagModel.TAG_PER_GROUP_LIMIT;
        HashMap<Integer, ArrayList<KeywhatTag>> tags = getTags().getValue();
        for(KeywhatTag tag : tags.get(gid)) {
            if(tag.getTid() == tid)
                tag.setSelected(false);
        }

        mSelectedCounter--;

        getTags().setValue(tags);
    }

    public int getNumberOfSelectedTags() {
        return mSelectedCounter;
    }
}
