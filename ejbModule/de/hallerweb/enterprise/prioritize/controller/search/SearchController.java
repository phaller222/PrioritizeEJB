/*
 * Copyright 2015-2024 Peter Michael Haller and contributors
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

package de.hallerweb.enterprise.prioritize.controller.search;

import de.hallerweb.enterprise.prioritize.controller.DepartmentController;
import de.hallerweb.enterprise.prioritize.controller.document.DocumentController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.controller.skill.SkillController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.document.DocumentInfo;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.search.PSearchable;
import de.hallerweb.enterprise.prioritize.model.search.SearchProperty;
import de.hallerweb.enterprise.prioritize.model.search.SearchResult;
import de.hallerweb.enterprise.prioritize.model.security.Role;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.skill.Skill;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * SerchController.java - Performs differnt kinds of searches on {@link PSearchable} objects.
 */
@Stateless
public class SearchController {

    @EJB
    UserRoleController userRoleController;
    @EJB
    DocumentController documentController;
    @EJB
    ResourceController resourceController;
    @EJB
    DepartmentController departmentController;
    @EJB
    SkillController skillController;
    @EJB
    AuthorizationController authController;

    @Inject
    SessionController sessionController;

    public List<SearchResult> searchUsers(String phrase, User sessionUser) {
        List<SearchResult> result = new ArrayList<>();
        List<User> users = userRoleController.getAllUsers(sessionUser);
        for (User user : users) {
            result.addAll(user.find(phrase));
        }
        return result;

    }

    public List<SearchResult> searchDocuments(String phrase, User user) {
        List<SearchResult> result = new ArrayList<>();
        List<DocumentInfo> documentInfos = documentController.getAllDocumentInfos(sessionController.getUser());
        for (DocumentInfo docInfo : documentInfos) {
            if (authController.canRead(docInfo, user)) {
                result.addAll(docInfo.find(phrase));
            }
        }
        return result;
    }

    public List<SearchResult> searchResources(String phrase, User user) {
        List<SearchResult> result = new ArrayList<>();
        List<Resource> resources = resourceController.getAllResources(sessionController.getUser());
        for (Resource res : resources) {
            if (authController.canRead(res, user)) {
                result.addAll(res.find(phrase));
            }
        }
        return result;
    }

    public List<SearchResult> searchSkills(String phrase, User user) {
        List<SearchResult> result = new ArrayList<>();
        List<Skill> skills = skillController.getAllSkills(user);
        if (skills != null && !skills.isEmpty()) {
            for (Skill skill : skills) {
                if (authController.canRead(skill, user)) {
                    result.addAll(skill.find(phrase));
                }
            }
        }
        return result;
    }

    public List<SearchResult> searchRoles(String phrase, User sessionUser) {
        List<SearchResult> result = new ArrayList<>();
        List<Role> roles = userRoleController.getAllRoles(sessionUser);
        for (Role role : roles) {
            result.addAll(role.find(phrase));
        }
        return result;
    }

    public List<SearchResult> searchDepartments(String phrase, User user) {
        List<SearchResult> result = new ArrayList<>();
        List<Department> departments = departmentController.getAllDepartments(sessionController.getUser());
        for (Department dept : departments) {
            if (authController.canRead(dept, user)) {
                result.addAll(dept.find(phrase));
            }
        }
        return result;
    }

    /**
     * Search all Users on the systems.
     *
     * @param phrase String the phrase to search for
     * @return List with SearchResults
     */
    public List<SearchResult> searchUser(String phrase, SearchProperty property, User sessionUser) {
        List<SearchResult> result = new ArrayList<>();
        List<User> users = userRoleController.getAllUsers(sessionUser);
        for (User user : users) {
            if (authController.canRead(user, sessionUser)) {
                result.addAll(user.find(phrase, property));
            }
        }
        return result;
    }


    public List<SearchResult> search(String phrase, User user) {
        List<SearchResult> result = new ArrayList<>();
        result.addAll(searchUsers(phrase, user));
        result.addAll(searchDocuments(phrase, user));
        result.addAll(searchResources(phrase, user));
        result.addAll(searchRoles(phrase, user));
        result.addAll(searchDepartments(phrase, user));
        result.addAll(searchSkills(phrase, user));
        return result;
    }

}
