/*
 * Copyright 2015 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.IdHolderCTE;
import com.blazebit.persistence.entity.IntIdEntity;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.entity.Version;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.category.NoH2;
import com.blazebit.persistence.testsuite.base.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.category.NoOracle;
import com.blazebit.persistence.testsuite.base.category.NoSQLite;
import com.blazebit.persistence.tx.TxVoidWork;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class DeleteTest extends AbstractCoreTest {

    Document doc1;
    Document doc2;
    Document doc3;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            Document.class,
            Version.class,
            IntIdEntity.class,
            Person.class, 
            IdHolderCTE.class
        };
    }
    
    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            doc1 = new Document("D1");
            doc2 = new Document("D2");
            doc3 = new Document("D3");

			Person o1 = new Person("P1");

			doc1.setOwner(o1);
			doc2.setOwner(o1);
			doc3.setOwner(o1);

			em.persist(o1);

			em.persist(doc1);
			em.persist(doc2);
			em.persist(doc3);

			em.flush();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testSimple() {
		final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d").where("d.name").eq("D1");
		String expected = "DELETE FROM Document d WHERE d.name = :param_0";

		assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
    			int updateCount = cb.executeUpdate();
    			assertEquals(1, updateCount);
            }
        });
	}

    /* Returning */
    
    // NOTE: H2 and MySQL only support returning generated keys
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testReturningAll() {
        final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d");
        cb.where("id").in(doc1.getId(), doc2.getId());
        String expected = "DELETE FROM Document d WHERE d.id IN (:param_0)";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                ReturningResult<Long> result = cb.executeWithReturning("id", Long.class);
                assertEquals(2, result.getUpdateCount());
                assertEquals(2, result.getResultList().size());
                List<Long> orderedList = new ArrayList<Long>(new TreeSet<Long>(result.getResultList()));
                assertEquals(doc1.getId(), orderedList.get(0));
                assertEquals(doc2.getId(), orderedList.get(1));
            }
        });
    }

    // NOTE: H2 and MySQL only support returning generated keys
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testReturningLast() {
        final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d");
        cb.where("id").in(doc1.getId(), doc2.getId());
        String expected = "DELETE FROM Document d WHERE d.id IN (:param_0)";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                ReturningResult<Long> result = cb.executeWithReturning("id", Long.class);
                assertEquals(2, result.getUpdateCount());
                List<Long> list = Arrays.asList(doc1.getId(), doc2.getId());
                assertTrue(list.contains(result.getLastResult()));
            }
        });
    }

    // NOTE: H2 only supports with clause in select statement
    // NOTE: MySQL does not support CTEs
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testReturningLastWithCte() {
        final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d");
        cb.with(IdHolderCTE.class)
            .from(Document.class, "subDoc")
            .bind("id").select("subDoc.id")
            .orderByAsc("subDoc.id")
            .setMaxResults(2)
        .end();
        cb.where("id").in()
            .from(IdHolderCTE.class, "idHolder")
            .select("idHolder.id")
        .end();
        String expected = "WITH IdHolderCTE(id) AS(\n"
            + "SELECT subDoc.id FROM Document subDoc ORDER BY " + renderNullPrecedence("subDoc.id", "ASC", "LAST") + " LIMIT 2\n"
            + ")\n"
            + "DELETE FROM Document d WHERE d.id IN (SELECT idHolder.id FROM IdHolderCTE idHolder)";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                ReturningResult<Long> result = cb.executeWithReturning("id", Long.class);
                assertEquals(2, result.getUpdateCount());
                List<Long> list = Arrays.asList(doc1.getId(), doc2.getId());
                assertTrue(list.contains(result.getLastResult()));
            }
        });
    }

    // NOTE: H2 only supports with clause in select statement
    // NOTE: MySQL does not support CTEs
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testSimpleWithCte() {
        final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d");
        cb.with(IdHolderCTE.class)
            .from(Document.class, "subDoc")
            .bind("id").select("subDoc.id")
            .orderByAsc("subDoc.id")
            .setMaxResults(2)
        .end();
        cb.where("id").in()
            .from(IdHolderCTE.class, "idHolder")
            .select("idHolder.id")
        .end();
        String expected = "WITH IdHolderCTE(id) AS(\n"
            + "SELECT subDoc.id FROM Document subDoc ORDER BY " + renderNullPrecedence("subDoc.id", "ASC", "LAST") + " LIMIT 2\n"
            + ")\n"
            + "DELETE FROM Document d WHERE d.id IN (SELECT idHolder.id FROM IdHolderCTE idHolder)";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                assertEquals(2, cb.executeUpdate());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testDeleteReturningSelectOld() {
        final CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.withReturning(IdHolderCTE.class)
            .delete(Document.class, "deletedDoc")
            .where("deletedDoc.id").eq(doc1.getId())
            .returning("id", "id")
        .end();
        cb.fromOld(Document.class, "doc");
        cb.from(IdHolderCTE.class, "idHolder");
        cb.select("doc");
        cb.where("doc.id").eqExpression("idHolder.id");
        
        String expected = "WITH IdHolderCTE(id) AS(\n"
            + "DELETE FROM Document deletedDoc WHERE deletedDoc.id = :param_0 RETURNING id\n"
            + ")\n"
            + "SELECT doc FROM Document doc, IdHolderCTE idHolder WHERE doc.id = idHolder.id";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                String name = cb.getSingleResult().getName();
                assertEquals("D1", name);
                em.clear();
                // Of course the next statement would see the changes
                assertNull(em.find(Document.class, doc1.getId()));
            }
        });
    }
    
    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testDeleteReturningSelectNew() {
        final CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.withReturning(IdHolderCTE.class)
            .delete(Document.class, "deletedDoc")
            .where("deletedDoc.id").eq(doc1.getId())
            .returning("id", "id")
        .end();
        cb.fromNew(Document.class, "doc");
        cb.from(IdHolderCTE.class, "idHolder");
        cb.select("doc");
        cb.where("doc.id").eqExpression("idHolder.id");
        
        String expected = "WITH IdHolderCTE(id) AS(\n"
            + "DELETE FROM Document deletedDoc WHERE deletedDoc.id = :param_0 RETURNING id\n"
            + ")\n"
            + "SELECT doc FROM Document doc, IdHolderCTE idHolder WHERE doc.id = idHolder.id";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                List<Document> resultList = cb.getResultList();
                assertTrue(resultList.isEmpty());
                em.clear();
                // Of course the next statement would see the changes
                assertNull(em.find(Document.class, doc1.getId()));
            }
        });
    }
}