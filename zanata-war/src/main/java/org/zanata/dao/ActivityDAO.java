/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ActivityType;
import org.zanata.model.Activity;
import org.zanata.model.type.EntityType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("activityDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class ActivityDAO extends AbstractDAOImpl<Activity, Long> {
    private static final long serialVersionUID = 1L;

    public ActivityDAO() {
        super(Activity.class);
    }

    public ActivityDAO(Session session) {
        super(Activity.class, session);
    }

    @SuppressWarnings("unchecked")
    public Activity findActivity(long personId, EntityType contextType,
            long contextId, ActivityType activityType, Date approxTime) {
        Query query =
                getSession().createQuery(
                        "FROM Activity a WHERE a.actor.id = :personId "
                                + "AND a.contextId = :contextId "
                                + "AND a.activityType = :activityType "
                                + "AND a.contextType = :contextType "
                                + "AND :approxTime = a.approxTime");
        query.setParameter("personId", personId);
        query.setParameter("contextId", contextId);
        query.setParameter("activityType", activityType);
        query.setParameter("contextType", contextType);
        query.setTimestamp("approxTime", approxTime);
        query.setCacheable(true);
        query.setComment("activityDAO.findActivity");
        return (Activity) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<Activity> findLatestVersionActivitiesByUser(long personId,
        List<Long> versionIds, int offset, int maxResults) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("FROM Activity a WHERE a.actor.id = :personId ");
        queryBuilder.append("AND a.contextType = 'HProjectIteration' ");
        queryBuilder.append("AND a.contextId in (:versionIds) ");
        queryBuilder.append("order by a.lastChanged DESC");

        Query query = getSession().createQuery(queryBuilder.toString());
        query.setParameter("personId", personId);
        query.setParameterList("versionIds", versionIds);
        query.setMaxResults(maxResults);
        query.setFirstResult(offset);
        query.setCacheable(true);
        query.setComment("activityDAO.findLatestVersionActivitiesByUser");
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Activity> findLatestVersionActivities(Long versionId,
            int offset, int maxResults) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("FROM Activity a WHERE a.contextType = 'HProjectIteration' ");
        queryBuilder.append("AND a.contextId = :versionId ");
        queryBuilder.append("order by a.lastChanged DESC");

        Query query = getSession().createQuery(queryBuilder.toString());
        query.setParameter("versionId", versionId);
        query.setMaxResults(maxResults);
        query.setFirstResult(offset);
        query.setCacheable(true);
        query.setComment("activityDAO.findLatestVersionActivities");
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Activity> findLatestActivitiesForContext(long personId,
            long contextId, int offset, int maxResults) {
        Query query =
                getSession().createQuery(
                        "FROM Activity a WHERE a.actor.id = :personId "
                                + "AND a.contextId = :contextId "
                                + "order by a.lastChanged DESC");
        query.setParameter("personId", personId);
        query.setParameter("contextId", contextId);
        query.setMaxResults(maxResults);
        query.setFirstResult(offset);
        query.setCacheable(true);
        query.setComment("activityDAO.findActivities");
        return query.list();
    }

    public List<Activity> findLatestActivities(long personId, int offset,
            int maxResults) {
        Query query =
                getSession().createQuery(
                        "FROM Activity a WHERE a.actor.id = :personId "
                                + "order by a.lastChanged DESC");
        query.setParameter("personId", personId);
        query.setMaxResults(maxResults);
        query.setFirstResult(offset);
        query.setCacheable(true);
        query.setComment("activityDAO.findLatestActivities");
        return (List<Activity>) query.list();
    }

    public int getActivityCountByActor(Long personId) {
        Query q =
                getSession()
                        .createQuery(
                                "select count(*) from Activity a where a.actor.id = :personId");
        q.setParameter("personId", personId);
        q.setCacheable(true);
        q.setComment("activityDAO.getActivityCountByActor");
        Long totalCount = (Long) q.uniqueResult();
        if (totalCount == null) {
            return 0;
        }
        return totalCount.intValue();
    }

    /**
     * Return int[0]: Words translated, int[1]: Messages translated, int[2]:
     * Documents translated
     *
     * @param personId
     * @param startDate
     * @param endDate
     * @return
     */
    public int[]
            getTranslatedStats(Long personId, Date startDate, Date endDate) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("select sum(tft.textFlow.wordCount),count(tft.textFlow)," +
                        "count(distinct tft.textFlow.document) " +
                        "from HTextFlowTarget tft ");
        queryBuilder.append("where tft.translator.id = :personId ");
        queryBuilder
                .append("and tft.lastChanged BETWEEN :startDate AND :endDate ");

        Query q = getSession().createQuery(queryBuilder.toString());
        q.setParameter("personId", personId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setCacheable(true);
        q.setComment("activityDAO.getTranslatedStats");

        Object[] objects = (Object[]) q.uniqueResult();

        int[] results = new int[] { 0, 0, 0 };
        if (objects.length < 1) {
            return results;
        }

        for (int i = 0; i < results.length; i++) {
            if (objects.length >= i) {
                Long count = (Long) objects[i];
                results[i] = count == null ? 0 : count.intValue();
            }
        }
        return results;
    }

    /**
     * Return int[0]: Words reviewed, int[1]: Messages reviewed, int[2]:
     * Documents reviewed
     *
     * @param personId
     * @param startDate
     * @param endDate
     * @return
     */
    public int[] getReviewedStats(Long personId, Date startDate, Date endDate) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("select sum(tf.wordCount),count(tf),count(distinct doc) from HTextFlow tf, HTextFlowTarget tft, HDocument doc ");
        queryBuilder.append("where tft.reviewer.id = :personId ");
        queryBuilder
                .append("and tft.lastChanged BETWEEN :startDate AND :endDate ");
        queryBuilder.append("and tf.id = tft.textFlow.id ");
        queryBuilder.append("and tf.document.id = doc.id ");

        Query q = getSession().createQuery(queryBuilder.toString());
        q.setParameter("personId", personId);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setCacheable(true);
        q.setComment("activityDAO.getReviewedStats");

        Object[] objects = (Object[]) q.uniqueResult();

        int[] results = new int[] { 0, 0, 0 };
        if (objects.length < 1) {
            return results;
        }

        for (int i = 0; i < 3; i++) {
            if (objects.length >= i) {
                Long count = (Long) objects[i];
                results[i] = count == null ? 0 : count.intValue();
            }
        }
        return results;
    }
}
