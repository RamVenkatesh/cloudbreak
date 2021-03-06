package com.sequenceiq.cloudbreak.domain;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sequenceiq.cloudbreak.api.model.ExecutionType;

@Entity
@Table(name = "recipe", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "account", "name" })
})
@NamedQueries({
        @NamedQuery(
                name = "Recipe.findForUser",
                query = "SELECT r FROM Recipe r "
                        + "WHERE r.owner= :owner"),
        @NamedQuery(
                name = "Recipe.findPublicInAccountForUser",
                query = "SELECT r FROM Recipe r "
                        + "WHERE (r.account= :account AND r.publicInAccount= true) "
                        + "OR r.owner= :owner"),
        @NamedQuery(
                name = "Recipe.findAllInAccount",
                query = "SELECT r FROM Recipe r "
                        + "WHERE r.account= :account "),
        @NamedQuery(
                name = "Recipe.findByNameForUser",
                query = "SELECT r FROM Recipe r "
                        + "WHERE r.name= :name and r.owner= :owner "),
        @NamedQuery(
                name = "Recipe.findByNameInAccount",
                query = "SELECT r FROM Recipe r WHERE r.name= :name and r.account= :account")
})
public class Recipe implements ProvisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "recipe_generator")
    @SequenceGenerator(name = "recipe_generator", sequenceName = "recipe_id_seq", allocationSize = 1)
    private Long id;

    private String name;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "plugin")
    @Column(name = "execution_type")
    @Enumerated(EnumType.STRING)
    private Map<String, ExecutionType> plugins;
    private Integer timeout;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "key")
    @Column(name = "value", columnDefinition = "TEXT", length = 100000)
    private Map<String, String> keyValues;

    private String account;

    private String owner;

    private boolean publicInAccount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(Map<String, String> keyValues) {
        this.keyValues = keyValues;
    }

    public Map<String, ExecutionType> getPlugins() {
        return plugins;
    }

    public void setPlugins(Map<String, ExecutionType> plugins) {
        this.plugins = plugins;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isPublicInAccount() {
        return publicInAccount;
    }

    public void setPublicInAccount(boolean publicInAccount) {
        this.publicInAccount = publicInAccount;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
