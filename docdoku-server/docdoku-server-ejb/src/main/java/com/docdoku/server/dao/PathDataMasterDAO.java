/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server.dao;

import com.docdoku.core.configuration.PathDataMaster;
import com.docdoku.core.configuration.ProductInstanceMaster;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathDataMasterDAO {

    private EntityManager em;
    private Locale mLocale;

    private static Logger LOGGER = Logger.getLogger(PathDataMasterDAO.class.getName());

    public PathDataMasterDAO(EntityManager pEM) {
        em = pEM;
    }

    public PathDataMasterDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public void createPathData(PathDataMaster pathDataMaster){
        try {
            em.persist(pathDataMaster);
            em.flush();
        }catch (Exception e){
            LOGGER.log(Level.SEVERE,"Fail to create path data",e);
        }
    }


    public PathDataMaster findByPathAndProductInstance(String path, ProductInstanceMaster productInstanceMaster){
        try {
            return em.createNamedQuery("pathDataMaster.findByPathAndProductInstanceMaster", PathDataMaster.class)
                    .setParameter("path", path)
                    .setParameter("productInstanceMaster", productInstanceMaster)
                    .getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }
    public PathDataMaster findByPathIdAndProductInstance(int pathId, ProductInstanceMaster productInstanceMaster){
        try {
            return em.createNamedQuery("pathDataMaster.findByPathIdAndProductInstanceMaster", PathDataMaster.class)
                    .setParameter("pathId", pathId)
                    .setParameter("productInstanceMaster", productInstanceMaster)
                    .getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public ProductInstanceMaster findByPathData(PathDataMaster pathDataMaster){
        try {
            return em.createNamedQuery("ProductInstanceMaster.findByPathData", ProductInstanceMaster.class)
                    .setParameter("pathDataMasterList", pathDataMaster)
                    .getSingleResult();
        }catch(NoResultException e){
            return null;
        }
    }

    public void removePathData(PathDataMaster pathDataMaster) {
        em.remove(pathDataMaster);
        em.flush();
    }
}