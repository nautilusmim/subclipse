/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2006 Subclipse project and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Subclipse project committers - initial API and implementation
 * ****************************************************************************
 */
package org.tigris.subversion.subclipse.ui.subscriber;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

public class ShowHistorySynchronizeAction extends SynchronizeModelAction {

  public ShowHistorySynchronizeAction(String text, ISynchronizePageConfiguration configuration) {
    super(text, configuration);
  }

  /* (non-Javadoc)
   * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSyncInfoFilter()
   */
  protected FastSyncInfoFilter getSyncInfoFilter() {
    return new FastSyncInfoFilter() {
      public boolean select(SyncInfo info) {
        IStructuredSelection selection = getStructuredSelection();
        if (selection.size() != 1) return false;
        ISynchronizeModelElement element = (ISynchronizeModelElement) selection.getFirstElement();
        IResource resource = element.getResource();
        if (resource == null) return false;
        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        try {
          return !resource.exists()
              || (svnResource.getStatus().isManaged() && !svnResource.getStatus().isAdded());
        } catch (SVNException e) {
          return false;
        }
      }
    };
  }

  protected SynchronizeModelOperation getSubscriberOperation(
      ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
    ISynchronizeModelElement element =
        (ISynchronizeModelElement) getStructuredSelection().getFirstElement();
    IResource resource = element.getResource();
    if (!resource.exists()) {
      try {
        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        return new ShowHistorySynchronizeOperation(
            configuration, elements, svnResource.getLatestRemoteResource());
      } catch (SVNException e) {
        SVNUIPlugin.log(IStatus.ERROR, e.getMessage(), e);
      }
    }
    return new ShowHistorySynchronizeOperation(configuration, elements, resource);
  }
}
