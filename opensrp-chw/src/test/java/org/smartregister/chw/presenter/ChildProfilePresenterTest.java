package org.smartregister.chw.presenter;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.smartregister.chw.BaseUnitTest;
import org.smartregister.chw.core.contract.CoreChildProfileContract;
import org.smartregister.chw.interactor.ChildProfileInteractor;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.UUID;

public class ChildProfilePresenterTest extends BaseUnitTest {

    private static final String testBaseEntityId = UUID.randomUUID().toString();
    @Mock
    private CoreChildProfileContract.Presenter childProfilePresenter;
    @Mock
    private CoreChildProfileContract.View childProfileView;
    @Mock
    private CoreChildProfileContract.Flavor flavor;
    @Mock
    private CoreChildProfileContract.Model childProfileModel;

    @Mock
    private CommonPersonObjectClient personObjectClient;

    @Mock
    private ChildProfileInteractor interactor;

    @Mock
    private Context context;

    @Mock
    private CoreChildProfileContract.View view;
    @Mock
    private CoreChildProfileContract.Model model;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        childProfilePresenter = new ChildProfilePresenter(childProfileView, flavor, childProfileModel, testBaseEntityId);
        interactor.setpClient(personObjectClient);
        ((ChildProfilePresenter) childProfilePresenter).setInteractor(interactor);
        Mockito.doReturn(context).when(childProfileView).getContext();
    }


    @Test
    public void testThatModelWasInitialized() {
        CoreChildProfileContract.Model model = ((ChildProfilePresenter) childProfilePresenter).getModel();
        Assert.assertNotNull(model);
    }

    @Test
    public void testThatViewlWasInitialized() {
        CoreChildProfileContract.View view = childProfilePresenter.getView();
        Assert.assertNotNull(view);
    }

    @Test
    public void testFetchVisitStatus() {
        interactor.setpClient(personObjectClient);
        childProfilePresenter.fetchVisitStatus(testBaseEntityId);
        Mockito.verify(interactor, Mockito.atLeastOnce()).refreshChildVisitBar(childProfileView.getContext(),
                testBaseEntityId, (ChildProfilePresenter) childProfilePresenter);
    }

    @Test
    public void testProcessBackGroundEvent() {
        childProfilePresenter.processBackGroundEvent();
        Mockito.verify(interactor, Mockito.atLeastOnce()).processBackGroundEvent((ChildProfilePresenter) childProfilePresenter);
    }

    @Test
    public void testRefreshProfileTopSection() {


        ChildProfilePresenter profilePresenter = new ChildProfilePresenter(view, flavor, model, "12345");
        Mockito.doReturn(context).when(view).getContext();

        Resources resources = Mockito.mock(Resources.class);
        Mockito.doReturn(resources).when(context).getResources();

        Mockito.doReturn("12345").when(resources).getString(Mockito.anyInt());
        Mockito.doReturn("String").when(context).getString(Mockito.anyInt());
        Mockito.doReturn("String").when(view).getString(Mockito.anyInt());
        CommonPersonObject personObject = Mockito.mock(CommonPersonObject.class);

        profilePresenter.refreshProfileTopSection(personObjectClient, personObject);

        Mockito.verify(view).setProfileName(Mockito.any());
        Mockito.verify(view).setAge(Mockito.any());
    }
}
