package org.smartregister.chw.presenter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.smartregister.chw.BaseUnitTest;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.GuideBooksFragmentContract;

import java.util.ArrayList;
import java.util.List;

public class GuideBooksFragmentPresenterTest extends BaseUnitTest {

    @Mock
    private GuideBooksFragmentContract.Interactor interactor;

    @Mock
    private GuideBooksFragmentContract.View view;

    private GuideBooksFragmentPresenter presenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        presenter = new GuideBooksFragmentPresenter(view, interactor);
    }


    @Test
    public void testInitialize() {
        presenter.initialize("files.json", ChwApplication.getGuideBooksDirectory());
        Mockito.verify(interactor).getFiles(null, "files.json", ChwApplication.getGuideBooksDirectory(),presenter);
    }

    @Test
    public void testGetView() {
        Assert.assertEquals(presenter.getView(), view);
    }

    @Test
    public void testOnDataFetched() {
        List<GuideBooksFragmentContract.RemoteFile> videos = new ArrayList<>();
        presenter.onDataFetched(videos);
        Mockito.verify(view).onDataReceived(videos);
    }
}
