/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsFile.java,v $
 * Date   : $Date: 2004/05/21 15:14:28 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * A file object in OpenCms.<p>
 * 
 * A file object is a CmsResource that contains an additional byte[] array 
 * of binary data, which is the file content. 
 * A file object is not allowed to have sub-resources.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.4 $
 */
public class CmsFile extends CmsResource implements Cloneable, Serializable, Comparable {

    /** The content of this file */
    private byte[] m_fileContent;

    /**
     * Constructor, creates a new CmsFile Object from the given CmsResource with 
     * an empty byte array as file content.<p>
     * 
     * @param resource the base resource object to create a file from
     */
    public CmsFile(CmsResource resource) {

        this(
            resource.getStructureId(),
            resource.getResourceId(),
            resource.getParentStructureId(),
            resource.getFileId(),
            resource.getName(),
            resource.getType(),
            resource.getFlags(),
            resource.getProjectLastModified(),
            resource.getState(),
            resource.getLoaderId(),
            resource.getDateCreated(),
            resource.getUserCreated(),
            resource.getDateLastModified(),
            resource.getUserLastModified(),
            resource.getDateReleased(),
            resource.getDateExpired(),
            resource.getLinkCount(),
            0,
            new byte[0]);
        if (resource.hasFullResourceName()) {
            setFullResourceName(resource.getRootPath());
        }
        if (resource instanceof CmsFile) {
            // the resource already was a file, keep contents that might have been read already
            m_fileContent = ((CmsFile)resource).getContents();
            if (m_fileContent == null) {
                m_fileContent = new byte[0];
            }
        }
    }

    /**
     * Constructor, creates a new CmsFile object.<p>
     *
     * @param structureId the id of this resources structure record
     * @param resourceId the id of this resources resource record
     * @param parentId the id of this resources parent folder
     * @param fileId the id of this resources content record
     * @param name the filename of this resouce
     * @param type the type of this resource
     * @param flags the flags of this resource
     * @param projectId the project id this resource was last modified in
     * @param state the state of this resource
     * @param loaderId the id for the that is used to load this recource
     * @param dateCreated the creation date of this resource
     * @param userCreated the id of the user who created this resource
     * @param dateLastModified the date of the last modification of this resource
     * @param userLastModified the id of the user who did the last modification of this resource
     * @param dateReleased the release date of this resource
     * @param dateExpired the expiration date of this resource
     * @param linkCount the count of all siblings of this resource 
     * @param length the size of the file content of this resource
     * @param content the binary content data of this file
     */
    public CmsFile(
        CmsUUID structureId,
        CmsUUID resourceId,
        CmsUUID parentId,
        CmsUUID fileId,
        String name,
        int type,
        int flags,
        int projectId,
        int state,
        int loaderId,
        long dateCreated,
        CmsUUID userCreated,
        long dateLastModified,
        CmsUUID userLastModified,
        long dateReleased,
        long dateExpired,
        int linkCount,
        int length,
        byte[] content) {

        // create the CmsResource.
        super(
            structureId,
            resourceId,
            parentId,
            fileId,
            name,
            type,
            flags,
            projectId,
            state,
            loaderId,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            linkCount,
            length);

        // set content and length
        m_fileContent = content;
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        byte[] newContent = new byte[this.getContents().length];
        System.arraycopy(getContents(), 0, newContent, 0, getContents().length);

        CmsFile clone = new CmsFile(
            getStructureId(),
            getResourceId(),
            getParentStructureId(),
            getFileId(),
            getName(),
            getType(),
            getFlags(),
            getProjectLastModified(),
            getState(),
            getLoaderId(),
            getDateCreated(),
            getUserCreated(),
            getDateLastModified(),
            getUserLastModified(),
            getDateReleased(),
            getDateExpired(),
            getLinkCount(),
            getLength(),
            newContent);

        return clone;
    }

    /**
     * Returns the content of this file.<p>
     *
     * @return the content of this file.
     */
    public byte[] getContents() {

        return m_fileContent;
    }

    /**
     * Sets the contents of this file.<p>
     *
     * @param value the content of this file.
     */
    public void setContents(byte[] value) {

        m_fileContent = value;
        if (m_fileContent.length > 0) {
            m_length = m_fileContent.length;
        } else {
            m_length = 0;
        }
    }

    /**
     * Utility method to upgrade a CmsResource to a CmsFile.<p>
     * 
     * Sometimes a CmsResource might already ba a (casted) CmsFile that
     * also has the contents read. This methods tries to optimize 
     * read access to the VFS by "upgrading" the CmsResource to a CmsFile 
     * first. If this fails, the CmsFile is read from the VFS.<p> 
     * 
     * @param resource the resource to upgrade
     * @param cms permission context for accessing the VFS
     * @return the upgraded (or read) file
     * @throws CmsException if something goes wrong
     */
    public static CmsFile upgrade(CmsResource resource, CmsObject cms) throws CmsException {

        if (resource instanceof CmsFile) {
            // resource is already a file
            CmsFile file = (CmsFile)resource;
            if ((file.getContents() != null) && (file.getContents().length > 0)) {
                // file has the contents already available
                return file;
            }
        }
        // resource is no file, or contents are not available
        String filename = cms.readAbsolutePath(resource);
        // read and return the file
        return cms.readFile(filename);
    }
}