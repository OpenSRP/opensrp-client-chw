package org.smartregister.chw.dao;

import net.sqlcipher.MatrixCursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.Repository;

import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class WashCheckDaoTest extends WashCheckDao {
    @Mock
    private Repository repository;

    @Mock
    private SQLiteDatabase database;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        setRepository(repository);
    }

    @Test
    public void testGetLastWashCheckDate() {
        Mockito.doReturn(database).when(repository).getReadableDatabase();

        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"wash_check_date"});
        matrixCursor.addRow(new Object[]{"1567636636641"});
        Mockito.doReturn(matrixCursor).when(database).rawQuery(Mockito.any(), Mockito.any());

        long washCheckDate = WashCheckDao.getLastWashCheckDate("12345");

        Mockito.verify(database).rawQuery(Mockito.anyString(), Mockito.any());
        Assert.assertEquals("1567636636641",String.valueOf(washCheckDate));
    }

    @Test
    public void testGetAllWashCheckVisits() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"visit_id"});
        matrixCursor.addRow(new Object[]{"123456"});
        Mockito.doReturn(matrixCursor).when(database).rawQuery(Mockito.any(), Mockito.any());

        List<String> allWashCheckVisits = WashCheckDao.getAllWashCheckVisits(database);

        Mockito.verify(database).rawQuery(Mockito.anyString(), Mockito.any());
        Assert.assertEquals("123456",String.valueOf(allWashCheckVisits.get(0)));
    }

    @Test
    public void testGetWashCheckDetails() {
        Mockito.doReturn(database).when(repository).getReadableDatabase();

        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"visit_id", "base_entity_id", "visit_key",
                "parent_code", "preprocessed_type", "details", "human_readable_details"});

        Mockito.doReturn(matrixCursor).when(database).rawQuery(Mockito.any(), Mockito.any());

        Map<String, VisitDetail> map = WashCheckDao.getWashCheckDetails(239872398L, "123456");


        Mockito.verify(database).rawQuery(Mockito.anyString(), Mockito.any());
        Assert.assertEquals(map.size(), 0);
    }
    @Test
    public void testUpdateWashCheckVisitDetails() {
        Mockito.doReturn(database).when(repository).getWritableDatabase();
        WashCheckDao.updateWashCheckVisitDetails(Long.parseLong("1567329933757"), "12345", "Yes", "Yes", "Yes");
        Mockito.verify(database).rawExecSQL(Mockito.anyString());
    }

    @Test
    public void testUpdateWashCheckVisits() {
        Mockito.doReturn(database).when(repository).getWritableDatabase();
        WashCheckDao.updateWashCheckVisits(Long.parseLong("1567329933757"), "12345", "sample_json");
        Mockito.verify(database).rawExecSQL(Mockito.anyString());
    }

    @Test
    public void testGetWashCheckEvents() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"visit_id", "base_entity_id", "visit_key",
                "parent_code", "preprocessed_type", "details", "human_readable_details"});

        Mockito.doReturn(matrixCursor).when(database).rawQuery(Mockito.any(), Mockito.any());

        List<EventClient> eventClients = WashCheckDao.getWashCheckEvents(database);


        Mockito.verify(database).rawQuery(Mockito.anyString(), Mockito.any());
        Assert.assertEquals(eventClients.size(), 0);
    }

}
