package com.api.demo.mongorepositories.applicationpackage.promotionmodellist;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import static com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionListStates.IN_PROGRESS;

@Getter
@Setter
@Document(collection="promotionmodellist")
public class PromotionModelList extends BasicEntity {

	@Indexed
	private String locationId;
	@Indexed
	private String promotionId;
	@Indexed
	private String listState;

	private List<String> modelIds;

	public PromotionModelList() {
		this.locationId = "";
		this.promotionId = "";
		this.listState = IN_PROGRESS.name();
		this.modelIds = new ArrayList<>();
	}
}
