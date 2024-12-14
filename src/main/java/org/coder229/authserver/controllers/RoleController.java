package org.coder229.authserver.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/roles",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleController {
}
