package at.fhv.Authors.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

public record Author(String name, String surname) {

}
