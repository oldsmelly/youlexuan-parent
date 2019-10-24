package com.youlexuan.pojogroup;

import com.youlexuan.pojo.TbSpecification;
import com.youlexuan.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

public class Specification implements Serializable {

    //规格的pojo
    private TbSpecification specification;
    //规格对应的规格项pojo
    private List<TbSpecificationOption> specificationOptList;

    public Specification() {
    }

    public Specification(TbSpecification specification, List<TbSpecificationOption> specificationOptList) {

        this.specification = specification;
        this.specificationOptList = specificationOptList;
    }

    public TbSpecification getSpecification() {

        return specification;
    }

    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }

    public List<TbSpecificationOption> getSpecificationOptList() {
        return specificationOptList;
    }

    public void setSpecificationOptList(List<TbSpecificationOption> specificationOptList) {
        this.specificationOptList = specificationOptList;
    }
}
