package cn.haizhi.market.main.bean.madao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class GroupInfoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public GroupInfoExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        protected void addCriterionForJDBCDate(String condition, Date value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value.getTime()), property);
        }

        protected void addCriterionForJDBCDate(String condition, List<Date> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            List<java.sql.Date> dateList = new ArrayList<java.sql.Date>();
            Iterator<Date> iter = values.iterator();
            while (iter.hasNext()) {
                dateList.add(new java.sql.Date(iter.next().getTime()));
            }
            addCriterion(condition, dateList, property);
        }

        protected void addCriterionForJDBCDate(String condition, Date value1, Date value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);
        }

        public Criteria andGroupInfoIdIsNull() {
            addCriterion("group_info_id is null");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdIsNotNull() {
            addCriterion("group_info_id is not null");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdEqualTo(String value) {
            addCriterion("group_info_id =", value, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdNotEqualTo(String value) {
            addCriterion("group_info_id <>", value, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdGreaterThan(String value) {
            addCriterion("group_info_id >", value, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdGreaterThanOrEqualTo(String value) {
            addCriterion("group_info_id >=", value, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdLessThan(String value) {
            addCriterion("group_info_id <", value, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdLessThanOrEqualTo(String value) {
            addCriterion("group_info_id <=", value, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdLike(String value) {
            addCriterion("group_info_id like", value, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdNotLike(String value) {
            addCriterion("group_info_id not like", value, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdIn(List<String> values) {
            addCriterion("group_info_id in", values, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdNotIn(List<String> values) {
            addCriterion("group_info_id not in", values, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdBetween(String value1, String value2) {
            addCriterion("group_info_id between", value1, value2, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupInfoIdNotBetween(String value1, String value2) {
            addCriterion("group_info_id not between", value1, value2, "groupInfoId");
            return (Criteria) this;
        }

        public Criteria andGroupNumIsNull() {
            addCriterion("group_num is null");
            return (Criteria) this;
        }

        public Criteria andGroupNumIsNotNull() {
            addCriterion("group_num is not null");
            return (Criteria) this;
        }

        public Criteria andGroupNumEqualTo(Integer value) {
            addCriterion("group_num =", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumNotEqualTo(Integer value) {
            addCriterion("group_num <>", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumGreaterThan(Integer value) {
            addCriterion("group_num >", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("group_num >=", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumLessThan(Integer value) {
            addCriterion("group_num <", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumLessThanOrEqualTo(Integer value) {
            addCriterion("group_num <=", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumIn(List<Integer> values) {
            addCriterion("group_num in", values, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumNotIn(List<Integer> values) {
            addCriterion("group_num not in", values, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumBetween(Integer value1, Integer value2) {
            addCriterion("group_num between", value1, value2, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumNotBetween(Integer value1, Integer value2) {
            addCriterion("group_num not between", value1, value2, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupDateIsNull() {
            addCriterion("group_date is null");
            return (Criteria) this;
        }

        public Criteria andGroupDateIsNotNull() {
            addCriterion("group_date is not null");
            return (Criteria) this;
        }

        public Criteria andGroupDateEqualTo(Date value) {
            addCriterionForJDBCDate("group_date =", value, "groupDate");
            return (Criteria) this;
        }

        public Criteria andGroupDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("group_date <>", value, "groupDate");
            return (Criteria) this;
        }

        public Criteria andGroupDateGreaterThan(Date value) {
            addCriterionForJDBCDate("group_date >", value, "groupDate");
            return (Criteria) this;
        }

        public Criteria andGroupDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("group_date >=", value, "groupDate");
            return (Criteria) this;
        }

        public Criteria andGroupDateLessThan(Date value) {
            addCriterionForJDBCDate("group_date <", value, "groupDate");
            return (Criteria) this;
        }

        public Criteria andGroupDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("group_date <=", value, "groupDate");
            return (Criteria) this;
        }

        public Criteria andGroupDateIn(List<Date> values) {
            addCriterionForJDBCDate("group_date in", values, "groupDate");
            return (Criteria) this;
        }

        public Criteria andGroupDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("group_date not in", values, "groupDate");
            return (Criteria) this;
        }

        public Criteria andGroupDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("group_date between", value1, value2, "groupDate");
            return (Criteria) this;
        }

        public Criteria andGroupDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("group_date not between", value1, value2, "groupDate");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}