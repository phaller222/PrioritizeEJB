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

package de.hallerweb.enterprise.prioritize.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity()
public class Location {

    @Id
    @GeneratedValue
    private int id;
    String name;
    double locationX;
    double locationY;

    public Location() {
        super();
    }

    public Location(double x, double y) {
        this.locationX = x;
        this.locationY = y;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return locationX;
    }

    public void setX(double x) {
        this.locationX = x;
    }

    public double getY() {
        return locationY;
    }

    public void setY(double y) {
        this.locationY = y;
    }

    public int getId() {
        return id;
    }

}
