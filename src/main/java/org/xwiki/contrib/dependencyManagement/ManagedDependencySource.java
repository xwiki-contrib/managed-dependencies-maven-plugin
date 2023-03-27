/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
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
package org.xwiki.contrib.dependencyManagement;

import org.apache.maven.shared.transfer.dependencies.DefaultDependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;

/**
 * Utility class to store the coordinates to managed dependency sources.
 *
 * @version $id$
 * @since 1.0
 */
public class ManagedDependencySource
{
    private String groupId;

    private String artifactId;

    private String version;

    private String type;

    private String classifier;

    public ManagedDependencySource()
    {

    }

    public ManagedDependencySource(String groupId, String artifactId, String version, String type, String classifier)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
    }

    public DependableCoordinate toDependableCoordinate()
    {
        DefaultDependableCoordinate dependableCoordinate = new DefaultDependableCoordinate();
        dependableCoordinate.setGroupId(getGroupId());
        dependableCoordinate.setArtifactId(getArtifactId());
        dependableCoordinate.setVersion(getVersion());
        dependableCoordinate.setType(getType());
        dependableCoordinate.setClassifier(getClassifier());

        return dependableCoordinate;
    }

    public final String getGroupId()
    {
        return groupId;
    }

    /**
     * @param groupId The groupId to be set.
     */
    public final void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public final String getArtifactId()
    {
        return artifactId;
    }

    /**
     * @param artifactId The artifactId to be set.
     */
    public final void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public final String getVersion()
    {
        return version;
    }

    /**
     * @param version The version to be set.
     */
    public final void setVersion( String version )
    {
        this.version = version;
    }

    public final String getType()
    {
        return type != null ? type : "jar";
    }

    /**
     * @param type The type to be set.
     */
    public void setType( String type )
    {
        this.type = type;
    }

    public final String getClassifier()
    {
        return classifier;
    }

    /**
     * @param classifier The classifier to be set.
     */
    public final void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    @Override
    public String toString()
    {
        StringBuilder sb =
            new StringBuilder().append( groupId ).append( ':' ).append( artifactId ).append( ':' ).append( getType() );

        if ( classifier != null )
        {
            sb.append( ':' ).append( classifier );
        }

        sb.append( ':' ).append( version );

        return sb.toString();
    }
}