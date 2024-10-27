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

package de.hallerweb.enterprise.prioritize.view.boundary;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * <p>
 * Copyright: (c) 2015-2024
 * </p>
 * <p>
 * Peter Haller
 * </p>
 *
 * @author peter
 */
@ApplicationPath("/api")
public class ApiApplication extends ResourceConfig {

    public ApiApplication() {
        super();

        register(MultiPartFeature.class);
        // Register your resource classes here
        register(DocumentService.class);

    }


}
