package org.smartregister.chw.presenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.smartregister.family.contract.FamilyProfileDueContract;

public class FamilyProfileDuePresenterTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private FamilyProfileDuePresenter presenter;

    @Mock
    private FamilyProfileDueContract.View view;

    @Spy
    private FamilyProfileDueContract.Model model;

    private String familyBaseEntityId = "familyBaseEntityId";

    @Before
    public void setUp() {
        String viewConfigurationIdentifier = "viewConfigurationIdentifier";
        presenter = new FamilyProfileDuePresenter(view, model, viewConfigurationIdentifier, familyBaseEntityId, "childBaseEntityId");
    }

    @Test
    public void testInitializeQueries() {
        presenter.initializeQueries("main");
        Mockito.verify(view).initializeQueryParams(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(view).initializeAdapter(Mockito.any());

        Mockito.verify(view).countExecute();
        Mockito.verify(view).filterandSortInInitializeQueries();
    }
}
