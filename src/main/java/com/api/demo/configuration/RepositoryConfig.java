package com.api.demo.configuration;

import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrder;
import com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObject;
import com.api.demo.mongorepositories.applicationpackage.leads.Lead;
import com.api.demo.mongorepositories.applicationpackage.notes.Note;
import com.api.demo.mongorepositories.applicationpackage.notifications.Notification;
import com.api.demo.mongorepositories.applicationpackage.pricesheets.PriceSheet;
import com.api.demo.mongorepositories.applicationpackage.promomodels.PromoModelList;
import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelList;
import com.api.demo.mongorepositories.applicationpackage.promotions.Promotion;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreement;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.stateforms.StateForm;
import com.api.demo.mongorepositories.applicationpackage.whitelist.WhiteList;
import com.api.demo.mongorepositories.filestore.StoredFile;
import com.api.demo.mongorepositories.users.User;
import com.api.demo.pricedata.repositories.models.Model;
import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.equations.EquationData;
import com.api.demo.pricedata.repositories.factories.Factory;
import com.api.demo.pricedata.repositories.variables.VariableData;
import com.api.demo.security.utils.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class RepositoryConfig implements RepositoryRestConfigurer {
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(User.class);
        config.exposeIdsFor(StoredFile.class);
        config.exposeIdsFor(Note.class);
        config.exposeIdsFor(Notification.class);
        config.exposeIdsFor(Lead.class);
        config.exposeIdsFor(PurchaseAgreement.class);
        config.exposeIdsFor(PriceSheet.class);
        config.exposeIdsFor(SalesOffice.class);
        config.exposeIdsFor(ChangeOrder.class);
        config.exposeIdsFor(Promotion.class);
        config.exposeIdsFor(PromoModelList.class);
        config.exposeIdsFor(StateForm.class);
        config.exposeIdsFor(WhiteList.class);
        config.exposeIdsFor(DeletedObject.class);
        config.exposeIdsFor(Model.class);
        config.exposeIdsFor(Factory.class);
        config.exposeIdsFor(PriceData.class);
        config.exposeIdsFor(EquationData.class);
        config.exposeIdsFor(VariableData.class);
        config.exposeIdsFor(PromotionModelList.class);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**");
            }
        };
    }
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }


}
