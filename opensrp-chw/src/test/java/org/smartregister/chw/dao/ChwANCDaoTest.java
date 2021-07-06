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
import org.smartregister.repository.Repository;

@RunWith(MockitoJUnitRunner.class)
public class ChwANCDaoTest extends ChwPNCDao {

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
    public void testGetLastVisitDate() {
        Mockito.doReturn(database).when(repository).getReadableDatabase();

        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"earliestVisitDate"});

        Mockito.doReturn(matrixCursor).when(database).rawQuery(Mockito.any(), Mockito.any());

        String visitState = ChwANCDao.getLastVisitDate("12345");

        Mockito.verify(database).rawQuery(Mockito.anyString(), Mockito.any());
        String empty = "";
        Assert.assertEquals(empty, visitState);
    }

    @Test
    public void testGetLastContactDate() {
        Mockito.doReturn(database).when(repository).getReadableDatabase();

        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"lastContactVisit"});

        Mockito.doReturn(matrixCursor).when(database).rawQuery(Mockito.any(), Mockito.any());

        String visitState = ChwANCDao.getLastContactDate("12345");

        Mockito.verify(database).rawQuery(Mockito.anyString(), Mockito.any());
        String empty = "";
        Assert.assertEquals(empty, visitState);
    }

}
